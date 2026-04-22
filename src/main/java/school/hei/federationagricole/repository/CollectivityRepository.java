package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.Collectivity;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.Structure;
import school.hei.federationagricole.entity.enums.CollectivityOccupation;
import school.hei.federationagricole.entity.enums.Gender;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class CollectivityRepository {
    private final Connection connection;

    public Collectivity save(Collectivity collectivity, List<Integer> memberIds,
                             Integer presidentId, Integer vicePresidentId,
                             Integer treasurerId, Integer secretaryId) {
        String insertCollectivitySql = """
            INSERT INTO collectivity (number, name, speciality, federation_approval,
                                      authorization_date, location, id_federation, creation_datetime)
            VALUES (?, ?, ?, ?, ?, ?, 1, now())
            RETURNING id
        """;

        String insertMemberSql = """
            INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date)
            VALUES (?, ?, ?::collectivity_occupation, ?)
        """;

        try {
            connection.setAutoCommit(false);

            int collectivityId;
            try (PreparedStatement stmt = connection.prepareStatement(insertCollectivitySql)) {
                stmt.setString(1, collectivity.getNumber());
                stmt.setString(2, collectivity.getName());
                stmt.setString(3, collectivity.getSpeciality());
                stmt.setBoolean(4, collectivity.isFederationApproval());
                stmt.setTimestamp(5, collectivity.getAuthorizationDate() != null
                        ? Timestamp.from(collectivity.getAuthorizationDate()) : null);
                stmt.setString(6, collectivity.getLocation());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    collectivityId = rs.getInt("id");
                } else {
                    throw new SQLException("Failed to insert collectivity, no ID returned.");
                }
            }

            try (PreparedStatement memberStmt = connection.prepareStatement(insertMemberSql)) {
                Timestamp now = Timestamp.from(Instant.now());
                for (Integer memberId : memberIds) {
                    String occupation = determineOccupation(
                            memberId, presidentId, vicePresidentId, treasurerId, secretaryId);
                    memberStmt.setInt(1, memberId);
                    memberStmt.setInt(2, collectivityId);
                    memberStmt.setString(3, occupation);
                    memberStmt.setTimestamp(4, now);
                    memberStmt.addBatch();
                }
                memberStmt.executeBatch();
            }

            connection.commit();
            return findById(collectivityId);

        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Failed to save collectivity with members", e);
        } finally {
            resetAutoCommit();
        }
    }

    public List<Collectivity> saveAll(List<Collectivity> collectivities,
                                      List<List<Integer>> memberIdsList,
                                      List<Integer> presidentIds,
                                      List<Integer> vicePresidentIds,
                                      List<Integer> treasurerIds,
                                      List<Integer> secretaryIds) {
        List<Collectivity> saved = new ArrayList<>();
        for (int i = 0; i < collectivities.size(); i++) {
            saved.add(save(collectivities.get(i), memberIdsList.get(i),
                    presidentIds.get(i), vicePresidentIds.get(i),
                    treasurerIds.get(i), secretaryIds.get(i)));
        }
        return saved;
    }

    public boolean existsByNumber(String number) {
        String sql = "SELECT 1 FROM collectivity WHERE number = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, number);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check collectivity number uniqueness", e);
        }
    }

    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM collectivity WHERE name = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check collectivity name uniqueness", e);
        }
    }

    public Collectivity assignIdentification(Integer id, String number, String name) {
        // The WHERE clause guards against the race condition:
        // it only updates rows where BOTH fields are still null.
        String sql = """
            UPDATE collectivity
               SET number = ?,
                   name   = ?
             WHERE id = ?
               AND number IS NULL
               AND name   IS NULL
        """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, number);
                stmt.setString(2, name);
                stmt.setInt(3, id);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    // Either the row doesn't exist or number/name are already set.
                    // The controller already checked existence and immutability,
                    // so this is a safety net for the race-condition scenario.
                    connection.rollback();
                    throw new RuntimeException(
                            "Could not assign identification: collectivity not found or already identified.");
                }
            }

            connection.commit();
            return findById(id);

        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Failed to assign identification to collectivity", e);
        } finally {
            resetAutoCommit();
        }
    }

    public Collectivity findById(Integer id) {
        String sql = """
            SELECT id, number, name, speciality, creation_datetime,
                   federation_approval, authorization_date, location
            FROM collectivity
            WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Collectivity collectivity = Collectivity.builder()
                        .id(rs.getInt("id"))
                        .number(rs.getString("number"))
                        .name(rs.getString("name"))
                        .speciality(rs.getString("speciality"))
                        .creationDatetime(rs.getTimestamp("creation_datetime").toInstant())
                        .federationApproval(rs.getBoolean("federation_approval"))
                        .authorizationDate(rs.getTimestamp("authorization_date") != null
                                ? rs.getTimestamp("authorization_date").toInstant() : null)
                        .location(rs.getString("location"))
                        .build();

                fetchMembersAndStructure(collectivity);
                return collectivity;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity", e);
        }
    }

    private void fetchMembersAndStructure(Collectivity collectivity) {
        String sql = """
            SELECT
                m.id, m.first_name, m.last_name, m.birth_date, m.enrolment_date,
                m.address, m.email, m.phone_number, m.profession, m.gender,
                mc.occupation
            FROM member_collectivity mc
            JOIN member m ON mc.id_member = m.id
            WHERE mc.id_collectivity = ?
              AND mc.end_date IS NULL
        """;

        List<Member> members   = new ArrayList<>();
        Structure structure    = Structure.builder().build();
        Map<Integer, Member> cache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivity.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Integer memberId = rs.getInt("id");

                Member member = cache.computeIfAbsent(memberId, mid -> {
                    try {
                        return Member.builder()
                                .id(mid)
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
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to map member", e);
                    }
                });

                members.add(member);

                String occ = rs.getString("occupation");
                switch (CollectivityOccupation.valueOf(occ)) {
                    case PRESIDENT      -> structure.setPresident(member);
                    case VICE_PRESIDENT -> structure.setVicePresident(member);
                    case TREASURER      -> structure.setTreasurer(member);
                    case SECRETARY      -> structure.setSecretary(member);
                    default             -> { /* SENIOR / JUNIOR : not in structure */ }
                }
            }

            collectivity.setMembers(members);
            collectivity.setStructure(structure);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch members and structure", e);
        }
    }

    private String determineOccupation(Integer memberId, Integer presidentId,
                                       Integer vicePresidentId, Integer treasurerId,
                                       Integer secretaryId) {
        if (memberId.equals(presidentId))      return "PRESIDENT";
        if (memberId.equals(vicePresidentId))  return "VICE_PRESIDENT";
        if (memberId.equals(treasurerId))      return "TREASURER";
        if (memberId.equals(secretaryId))      return "SECRETARY";
        return hasMinimumSeniority(memberId) ? "SENIOR" : "JUNIOR";
    }

    private boolean hasMinimumSeniority(Integer memberId) {
        String sql = "SELECT enrolment_date FROM member WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp enrolmentDate = rs.getTimestamp("enrolment_date");
                long monthsBetween = ChronoUnit.MONTHS.between(
                        enrolmentDate.toLocalDateTime(), LocalDateTime.now());
                return monthsBetween >= 6;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check member seniority", e);
        }
    }

    private void rollback() {
        try { connection.rollback(); }
        catch (SQLException ex) { throw new RuntimeException("Failed to rollback transaction", ex); }
    }

    private void resetAutoCommit() {
        try { connection.setAutoCommit(true); }
        catch (SQLException e) { throw new RuntimeException("Failed to reset auto-commit", e); }
    }
}