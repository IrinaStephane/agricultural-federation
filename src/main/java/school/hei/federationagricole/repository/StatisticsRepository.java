package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
@AllArgsConstructor
public class StatisticsRepository {

    private final Connection connection;

    public Map<String, BigDecimal> getEarnedAmountByMember(String collectivityId,
                                                           LocalDate from, LocalDate to) {
        String sql = """
                SELECT t.id_member, COALESCE(SUM(t.amount), 0) AS earned
                FROM transaction t
                WHERE t.id_collectivity = ?
                  AND DATE(t.transaction_date) BETWEEN ? AND ?
                  AND t.transaction_type = 'IN'
                GROUP BY t.id_member
                """;
        Map<String, BigDecimal> result = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("id_member"), rs.getBigDecimal("earned"));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute earned amounts", e);
        }
    }

    /**
     * Returns the expected amount a member should have paid for a given active fee in the period.
     * Monthly: count of months in [max(from, eligibleFrom), to] * amount
     * Annual / Punctual: if eligible_from is in [from,to], 1 * amount, else 0
     */
    public Map<String, BigDecimal> getUnpaidAmountByMember(String collectivityId,
                                                           LocalDate from, LocalDate to) {
        String feesSql = """
                SELECT frequency, amount, eligible_from
                FROM membership_fee
                WHERE id_collectivity = ? AND is_active = true
                """;

        String membersSql = """
                SELECT DISTINCT id_member FROM member_collectivity
                WHERE id_collectivity = ? AND end_date IS NULL
                """;

        String totalPaidSql = """
                SELECT id_member, COALESCE(SUM(amount), 0) AS paid
                FROM transaction
                WHERE id_collectivity = ?
                  AND DATE(transaction_date) BETWEEN ? AND ?
                  AND transaction_type = 'IN'
                GROUP BY id_member
                """;

        try {
            BigDecimal totalExpected = BigDecimal.ZERO;
            try (PreparedStatement stmt = connection.prepareStatement(feesSql)) {
                stmt.setString(1, collectivityId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String freq = rs.getString("frequency");
                    BigDecimal amt = rs.getBigDecimal("amount");
                    java.sql.Date ef = rs.getDate("eligible_from");
                    LocalDate eligibleFrom = ef != null ? ef.toLocalDate() : from;
                    totalExpected = totalExpected.add(computeExpected(freq, amt, eligibleFrom, from, to));
                }
            }

            List<String> memberIds = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(membersSql)) {
                stmt.setString(1, collectivityId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) memberIds.add(rs.getString("id_member"));
            }

            Map<String, BigDecimal> paidByMember = new HashMap<>();
            try (PreparedStatement stmt = connection.prepareStatement(totalPaidSql)) {
                stmt.setString(1, collectivityId);
                stmt.setDate(2, Date.valueOf(from));
                stmt.setDate(3, Date.valueOf(to));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    paidByMember.put(rs.getString("id_member"), rs.getBigDecimal("paid"));
                }
            }

            Map<String, BigDecimal> result = new HashMap<>();
            for (String memberId : memberIds) {
                BigDecimal paid = paidByMember.getOrDefault(memberId, BigDecimal.ZERO);
                BigDecimal unpaid = totalExpected.subtract(paid);
                result.put(memberId, unpaid.max(BigDecimal.ZERO));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute unpaid amounts", e);
        }
    }

    private BigDecimal computeExpected(String frequency, BigDecimal amount,
                                       LocalDate eligibleFrom, LocalDate from, LocalDate to) {
        return switch (frequency) {
            case "MONTHLY" -> {
                // count months between max(from, eligibleFrom) and to
                LocalDate start = eligibleFrom.isAfter(from) ? eligibleFrom : from;
                if (start.isAfter(to)) yield BigDecimal.ZERO;
                long months = java.time.temporal.ChronoUnit.MONTHS.between(start.withDayOfMonth(1),
                        to.withDayOfMonth(1)) + 1;
                yield amount.multiply(BigDecimal.valueOf(months));
            }
            case "ANNUALLY", "PUNCTUALLY", "WEEKLY" -> {
                // due if eligible_from is within the period
                if (!eligibleFrom.isBefore(from) && !eligibleFrom.isAfter(to))
                    yield amount;
                yield BigDecimal.ZERO;
            }
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Returns count of new members (enrolment_date in period) per collectivity.
     */
    public Map<String, Integer> getNewMemberCountByCollectivity(LocalDate from, LocalDate to) {
        String sql = """
                SELECT mc.id_collectivity, COUNT(DISTINCT m.id) AS cnt
                FROM member_collectivity mc
                JOIN member m ON mc.id_member = m.id
                WHERE DATE(m.enrolment_date) BETWEEN ? AND ?
                AND mc.occupation = 'JUNIOR'
                GROUP BY mc.id_collectivity
                """;
        Map<String, Integer> result = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("id_collectivity"), rs.getInt("cnt"));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count new members", e);
        }
    }

    /**
     * Returns the percentage of members who are up-to-date with their dues,
     * per collectivity, in the given period.
     * A member is "up-to-date" if their unpaidAmount == 0.
     */
    public Map<String, Double> getMemberCurrentDuePercentageByCollectivity(LocalDate from, LocalDate to) {
        // Get all collectivities
        String colSql = "SELECT id FROM collectivity";
        Map<String, Double> result = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(colSql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String colId = rs.getString("id");
                double pct = computeCurrentDuePercentage(colId, from, to);
                result.put(colId, pct);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute due percentage", e);
        }
    }

    private double computeCurrentDuePercentage(String collectivityId, LocalDate from, LocalDate to) {
        Map<String, BigDecimal> unpaid = getUnpaidAmountByMember(collectivityId, from, to);
        if (unpaid.isEmpty()) return 100.0;
        long upToDate = unpaid.values().stream()
                .filter(v -> v.compareTo(BigDecimal.ZERO) <= 0)
                .count();
        return (double) upToDate / (double) unpaid.size() * 100.0;
    }

    /**
     * Returns all collectivity ids.
     */
    public List<String> findAllCollectivityIds() {
        String sql = "SELECT id FROM collectivity ORDER BY id";
        List<String> ids = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getString("id"));
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list collectivity ids", e);
        }
    }

    /**
     * Returns a compact info row (id, name, number) for a collectivity.
     */
    public Object[] findCollectivityInfo(String collectivityId) {
        String sql = "SELECT id, name, number FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return new Object[]{rs.getString("id"), rs.getString("name"), rs.getString("number")};
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity info", e);
        }
    }

    /**
     * Returns member info for a specific member in a collectivity (for MemberDescription).
     */
    public Map<String, Object[]> findMembersInfoByCollectivity(String collectivityId) {
        String sql = """
                SELECT m.id, m.first_name, m.last_name, m.email, mc.occupation
                FROM member_collectivity mc
                JOIN member m ON mc.id_member = m.id
                WHERE mc.id_collectivity = ? AND mc.end_date IS NULL
                """;
        Map<String, Object[]> result = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("id"), new Object[]{
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("occupation")
                });
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find members info", e);
        }
    }
}