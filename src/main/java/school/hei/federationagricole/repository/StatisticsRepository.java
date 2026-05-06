package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.MembershipFee;
import school.hei.federationagricole.entity.enums.ActivityStatus;
import school.hei.federationagricole.entity.enums.Frequency;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
@AllArgsConstructor
public class StatisticsRepository {

    private final Connection connection;

    // -----------------------------------------------------------------------
    //  LOCAL STATISTICS (per collectivity)
    // -----------------------------------------------------------------------

    /**
     * Returns each active member of the collectivity with their occupation.
     * Result: rows of (member_id, first_name, last_name, email, occupation)
     */
    public List<Map<String, Object>> findActiveMembersWithOccupation(Integer collectivityId) {
        String sql = """
            SELECT m.id, m.first_name, m.last_name, m.email, mc.occupation
            FROM member_collectivity mc
            JOIN member m ON mc.id_member = m.id
            WHERE mc.id_collectivity = ?
              AND mc.end_date IS NULL
            ORDER BY m.id
            """;

        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("firstName", rs.getString("first_name"));
                row.put("lastName", rs.getString("last_name"));
                row.put("email", rs.getString("email"));
                row.put("occupation", rs.getString("occupation"));
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch active members with occupation", e);
        }
    }

    /**
     * Returns the total amount paid by a member to a collectivity during [from, to].
     */
    public BigDecimal findEarnedAmountByMember(Integer memberId, Integer collectivityId,
                                               LocalDate from, LocalDate to) {
        String sql = """
            SELECT COALESCE(SUM(t.amount), 0) AS total
            FROM transaction t
            WHERE t.id_member = ?
              AND t.id_collectivity = ?
              AND DATE(t.transaction_date) BETWEEN ? AND ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, collectivityId);
            stmt.setDate(3, Date.valueOf(from));
            stmt.setDate(4, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch earned amount for member", e);
        }
    }

    /**
     * Returns the total amount paid by a member for a specific membership fee during [from, to].
     */
    public BigDecimal findPaidAmountByMemberAndFee(Integer memberId, Integer membershipFeeId,
                                                   LocalDate from, LocalDate to) {
        String sql = """
            SELECT COALESCE(SUM(t.amount), 0) AS total
            FROM transaction t
            WHERE t.id_member = ?
              AND t.id_membership_fee = ?
              AND DATE(t.transaction_date) BETWEEN ? AND ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, membershipFeeId);
            stmt.setDate(3, Date.valueOf(from));
            stmt.setDate(4, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch paid amount for member + fee", e);
        }
    }

    /**
     * Returns all ACTIVE membership fees for a collectivity.
     */
    public List<MembershipFee> findActiveFeesByCollectivity(Integer collectivityId) {
        String sql = """
            SELECT id, id_collectivity, label, frequency, amount, eligible_from, is_active
            FROM membership_fee
            WHERE id_collectivity = ?
              AND is_active = true
            ORDER BY id
            """;
        List<MembershipFee> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(MembershipFee.builder()
                        .id(rs.getInt("id"))
                        .collectivityId(rs.getInt("id_collectivity"))
                        .label(rs.getString("label"))
                        .frequency(Frequency.valueOf(rs.getString("frequency")))
                        .amount(rs.getBigDecimal("amount"))
                        .eligibleFrom(rs.getDate("eligible_from") != null
                                ? rs.getDate("eligible_from").toLocalDate() : null)
                        .status(ActivityStatus.ACTIVE)
                        .build());
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch active fees", e);
        }
    }

    // -----------------------------------------------------------------------
    //  OVERALL STATISTICS (federation-wide, all collectivities)
    // -----------------------------------------------------------------------

    /**
     * Returns all collectivities with id, name, number.
     */
    public List<Map<String, Object>> findAllCollectivities() {
        String sql = """
            SELECT id, name, number
            FROM collectivity
            ORDER BY id
            """;
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("name", rs.getString("name"));
                row.put("number", rs.getString("number"));
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all collectivities", e);
        }
    }

    /**
     * Returns the number of new members who joined a collectivity during [from, to].
     * "Joining" is defined as the start_date of the member_collectivity record.
     */
    public int countNewMembers(Integer collectivityId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM member_collectivity
            WHERE id_collectivity = ?
              AND DATE(start_date) BETWEEN ? AND ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("cnt") : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count new members", e);
        }
    }

    /**
     * Returns how many active members exist in a collectivity (end_date IS NULL).
     */
    public int countActiveMembers(Integer collectivityId) {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM member_collectivity
            WHERE id_collectivity = ?
              AND end_date IS NULL
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("cnt") : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count active members", e);
        }
    }

    /**
     * Returns the list of member IDs belonging to the collectivity (active).
     */
    public List<Integer> findActiveMemberIds(Integer collectivityId) {
        String sql = """
            SELECT id_member
            FROM member_collectivity
            WHERE id_collectivity = ?
              AND end_date IS NULL
            """;
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("id_member"));
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch active member IDs", e);
        }
    }
}