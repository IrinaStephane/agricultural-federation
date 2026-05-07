package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.ActivityAttendance;
import school.hei.federationagricole.entity.CollectivityActivity;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.dto.CreateActivityMemberAttendance;
import school.hei.federationagricole.entity.dto.CreateCollectivityActivity;
import school.hei.federationagricole.entity.enums.ActivityType;
import school.hei.federationagricole.entity.enums.AttendanceStatus;
import school.hei.federationagricole.entity.enums.CollectivityOccupation;
import school.hei.federationagricole.entity.enums.Gender;
import school.hei.federationagricole.entity.enums.WeekDay;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
@AllArgsConstructor
public class ActivityRepository {

    private final Connection connection;

    // ── Activities ──────────────────────────────────────────────────────────

    public List<CollectivityActivity> findByCollectivityId(String collectivityId) {
        String sql = """
            SELECT a.id, a.id_collectivity, a.label, a.activity_type,
                   a.executive_date, a.recurrence_week_ordinal, a.recurrence_day_of_week
            FROM collectivity_activity a
            WHERE a.id_collectivity = ?
            ORDER BY a.executive_date NULLS LAST, a.id
            """;
        List<CollectivityActivity> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CollectivityActivity activity = mapActivity(rs);
                activity.setMemberOccupationConcerned(findOccupationsByActivityId(activity.getId()));
                result.add(activity);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list activities", e);
        }
    }

    public CollectivityActivity findActivityById(String activityId) {
        String sql = """
            SELECT id, id_collectivity, label, activity_type,
                   executive_date, recurrence_week_ordinal, recurrence_day_of_week
            FROM collectivity_activity WHERE id = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            CollectivityActivity a = mapActivity(rs);
            a.setMemberOccupationConcerned(findOccupationsByActivityId(activityId));
            return a;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find activity", e);
        }
    }

    public List<CollectivityActivity> saveAll(String collectivityId, List<CreateCollectivityActivity> dtos) {
        String insertSql = """
            INSERT INTO collectivity_activity
                (id, id_collectivity, label, activity_type,
                 executive_date, recurrence_week_ordinal, recurrence_day_of_week)
            VALUES (?, ?, ?, ?::activity_type, ?, ?, ?::week_day)
            """;
        String insertOccSql = """
            INSERT INTO activity_occupation_concerned (id_activity, occupation)
            VALUES (?, ?::collectivity_occupation)
            """;
        List<CollectivityActivity> created = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            for (CreateCollectivityActivity dto : dtos) {
                String actId = "act-" + UUID.randomUUID().toString().substring(0, 8);
                try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                    stmt.setString(1, actId);
                    stmt.setString(2, collectivityId);
                    stmt.setString(3, dto.getLabel());
                    stmt.setString(4, dto.getActivityType().name());
                    if (dto.getExecutiveDate() != null)
                        stmt.setDate(5, java.sql.Date.valueOf(dto.getExecutiveDate()));
                    else
                        stmt.setNull(5, Types.DATE);
                    if (dto.getRecurrenceWeekOrdinal() != null)
                        stmt.setInt(6, dto.getRecurrenceWeekOrdinal());
                    else
                        stmt.setNull(6, Types.INTEGER);
                    if (dto.getRecurrenceDayOfWeek() != null)
                        stmt.setString(7, dto.getRecurrenceDayOfWeek().name());
                    else
                        stmt.setNull(7, Types.OTHER);
                    stmt.executeUpdate();
                }
                // Insert occupations concerned
                if (dto.getMemberOccupationConcerned() != null && !dto.getMemberOccupationConcerned().isEmpty()) {
                    try (PreparedStatement occStmt = connection.prepareStatement(insertOccSql)) {
                        for (CollectivityOccupation occ : dto.getMemberOccupationConcerned()) {
                            occStmt.setString(1, actId);
                            occStmt.setString(2, occ.name());
                            occStmt.addBatch();
                        }
                        occStmt.executeBatch();
                    }
                }
                created.add(findActivityById(actId));
            }
            connection.commit();
            return created;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new RuntimeException("Failed to save activities: " + e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }

    // ── Attendance ──────────────────────────────────────────────────────────

    public List<ActivityAttendance> findAttendanceByActivityId(String activityId) {
        String sql = """
            SELECT aa.id, aa.id_activity, aa.attendance_status, aa.occurrence_date,
                   m.id AS m_id, m.first_name, m.last_name, m.birth_date, m.enrolment_date,
                   m.address, m.email, m.phone_number, m.profession, m.gender
            FROM activity_attendance aa
            JOIN member m ON aa.id_member = m.id
            WHERE aa.id_activity = ?
            ORDER BY aa.occurrence_date, aa.id
            """;
        List<ActivityAttendance> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(ActivityAttendance.builder()
                        .id(rs.getString("id"))
                        .activityId(rs.getString("id_activity"))
                        .attendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")))
                        .member(mapMember(rs))
                        .occurrenceDate(rs.getDate("occurrence_date").toLocalDate())
                        .build());
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list attendance", e);
        }
    }

    /**
     * Save/update attendance. Once ATTENDED or MISSING is set, it cannot be changed.
     * UNDEFINED can always be updated.
     */
    public List<ActivityAttendance> saveAttendance(String activityId,
                                                   List<CreateActivityMemberAttendance> dtos) {
        String upsertSql = """
            INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status, occurrence_date)
            VALUES (?, ?, ?, ?::attendance_status, ?)
            ON CONFLICT (id_activity, id_member, occurrence_date) DO UPDATE
              SET attendance_status = EXCLUDED.attendance_status
              WHERE activity_attendance.attendance_status = 'UNDEFINED'
            """;
        List<ActivityAttendance> result = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            for (CreateActivityMemberAttendance dto : dtos) {
                String attId = "att-" + UUID.randomUUID().toString().substring(0, 8);
                try (PreparedStatement stmt = connection.prepareStatement(upsertSql)) {
                    LocalDate occDate = dto.getOccurrenceDate() != null ? dto.getOccurrenceDate() : LocalDate.now();
                    stmt.setString(1, attId);
                    stmt.setString(2, activityId);
                    stmt.setString(3, dto.getMemberIdentifier());
                    stmt.setString(4, dto.getAttendanceStatus().name());
                    stmt.setDate(5, java.sql.Date.valueOf(occDate));
                    stmt.executeUpdate();
                }
            }
            connection.commit();
            // Return final state from DB for all submitted members
            for (CreateActivityMemberAttendance dto : dtos) {
                ActivityAttendance att = findAttendanceByActivityAndMember(activityId, dto.getMemberIdentifier());
                if (att != null) result.add(att);
            }
            return result;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new RuntimeException("Failed to save attendance: " + e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }

    public ActivityAttendance findAttendanceByActivityAndMember(String activityId, String memberId) {
        String sql = """
            SELECT aa.id, aa.id_activity, aa.attendance_status,aa.occurrence_date,
                   m.id AS m_id, m.first_name, m.last_name, m.birth_date, m.enrolment_date,
                   m.address, m.email, m.phone_number, m.profession, m.gender
            FROM activity_attendance aa
            JOIN member m ON aa.id_member = m.id
            WHERE aa.id_activity = ? AND aa.id_member = ?
            ORDER BY aa.occurrence_date, aa.id
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            stmt.setString(2, memberId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return ActivityAttendance.builder()
                    .id(rs.getString("id"))
                    .activityId(rs.getString("id_activity"))
                    .attendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")))
                    .occurrenceDate(rs.getDate("occurrence_date").toLocalDate())
                    .member(mapMember(rs))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find attendance", e);
        }
    }

    /**
     * Returns attendance rate (0.0 – 1.0) for a member within a collectivity,
     * considering only activities of that collectivity where the member was concerned.
     */
    public Double getMemberAttendanceRate(String memberId, String collectivityId,
                                          java.time.LocalDate from, java.time.LocalDate to) {
        // Count total activities in period where member was concerned (by occupation or all-members)
        String totalSql = """
            SELECT COUNT(DISTINCT a.id) AS total
            FROM collectivity_activity a
            WHERE a.id_collectivity = ?
              AND (a.executive_date BETWEEN ? AND ?
                   OR a.executive_date IS NULL)
              AND (
                NOT EXISTS (SELECT 1 FROM activity_occupation_concerned aoc WHERE aoc.id_activity = a.id)
                OR EXISTS (
                    SELECT 1 FROM activity_occupation_concerned aoc
                    JOIN member_collectivity mc ON mc.id_member = ? AND mc.id_collectivity = ?
                    WHERE aoc.id_activity = a.id AND aoc.occupation = mc.occupation
                      AND mc.end_date IS NULL
                )
              )
            """;
        String attendedSql = """
            SELECT COUNT(*) AS attended
            FROM activity_attendance aa
            JOIN collectivity_activity a ON aa.id_activity = a.id
            WHERE a.id_collectivity = ?
              AND aa.id_member = ?
              AND aa.attendance_status = 'ATTENDED'
              AND (a.executive_date BETWEEN ? AND ? OR a.executive_date IS NULL)
            """;
        try {
            long total;
            try (PreparedStatement stmt = connection.prepareStatement(totalSql)) {
                stmt.setString(1, collectivityId);
                stmt.setDate(2, java.sql.Date.valueOf(from));
                stmt.setDate(3, java.sql.Date.valueOf(to));
                stmt.setString(4, memberId);
                stmt.setString(5, collectivityId);
                ResultSet rs = stmt.executeQuery();
                total = rs.next() ? rs.getLong("total") : 0L;
            }
            if (total == 0) return null;
            long attended;
            try (PreparedStatement stmt = connection.prepareStatement(attendedSql)) {
                stmt.setString(1, collectivityId);
                stmt.setString(2, memberId);
                stmt.setDate(3, java.sql.Date.valueOf(from));
                stmt.setDate(4, java.sql.Date.valueOf(to));
                ResultSet rs = stmt.executeQuery();
                attended = rs.next() ? rs.getLong("attended") : 0L;
            }
            return (double) attended / (double) total * 100.0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute attendance rate", e);
        }
    }

    /**
     * Returns overall attendance rate for all members of a collectivity in a period.
     */
    public Double getCollectivityAttendanceRate(String collectivityId,
                                                java.time.LocalDate from, java.time.LocalDate to) {
        String sql = """
            SELECT
                COUNT(CASE WHEN aa.attendance_status = 'ATTENDED' THEN 1 END) AS attended,
                COUNT(CASE WHEN aa.attendance_status IN ('ATTENDED','MISSING') THEN 1 END) AS total
            FROM activity_attendance aa
            JOIN collectivity_activity a ON aa.id_activity = a.id
            JOIN member_collectivity mc ON aa.id_member = mc.id_member
              AND mc.id_collectivity = a.id_collectivity AND mc.end_date IS NULL
            WHERE a.id_collectivity = ?
              AND (a.executive_date BETWEEN ? AND ? OR a.executive_date IS NULL)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setDate(2, java.sql.Date.valueOf(from));
            stmt.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            long total = rs.getLong("total");
            if (total == 0) return null;
            long attended = rs.getLong("attended");
            return (double) attended / (double) total * 100.0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute collectivity attendance rate", e);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private List<CollectivityOccupation> findOccupationsByActivityId(String activityId) {
        String sql = "SELECT occupation FROM activity_occupation_concerned WHERE id_activity = ?";
        List<CollectivityOccupation> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(CollectivityOccupation.valueOf(rs.getString("occupation")));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find occupations for activity", e);
        }
    }

    private CollectivityActivity mapActivity(ResultSet rs) throws SQLException {
        String dayStr = rs.getString("recurrence_day_of_week");
        java.sql.Date execDate = rs.getDate("executive_date");
        return CollectivityActivity.builder()
                .id(rs.getString("id"))
                .collectivityId(rs.getString("id_collectivity"))
                .label(rs.getString("label"))
                .activityType(ActivityType.valueOf(rs.getString("activity_type")))
                .executiveDate(execDate != null ? execDate.toLocalDate() : null)
                .recurrenceWeekOrdinal(rs.getObject("recurrence_week_ordinal") != null
                        ? rs.getInt("recurrence_week_ordinal") : null)
                .recurrenceDayOfWeek(dayStr != null ? WeekDay.valueOf(dayStr) : null)
                .build();
    }

    private Member mapMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getString("m_id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .birthDate(rs.getDate("birth_date").toLocalDate())
                .enrolmentDate(rs.getTimestamp("enrolment_date").toInstant())
                .address(rs.getString("address"))
                .email(rs.getString("email"))
                .phoneNumber(rs.getString("phone_number"))
                .profession(rs.getString("profession"))
                .gender(Gender.valueOf(rs.getString("gender")))
                .build();
    }
}