package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.MembershipFee;
import school.hei.federationagricole.entity.enums.ActivityStatus;
import school.hei.federationagricole.entity.enums.Frequency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class MembershipFeeRepository {

    private final Connection connection;

    public List<MembershipFee> findByCollectivityId(Integer collectivityId) {
        String sql = """
            SELECT id, id_collectivity, label, frequency, amount, eligible_from, is_active
            FROM membership_fee
            WHERE id_collectivity = ?
            ORDER BY id
            """;
        List<MembershipFee> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find membership fees", e);
        }
    }

    public MembershipFee findById(Integer id) {
        String sql = "SELECT id, id_collectivity, label, frequency, amount, eligible_from, is_active FROM membership_fee WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find membership fee by id", e);
        }
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM membership_fee WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check membership fee existence", e);
        }
    }

    public List<MembershipFee> saveAll(Integer collectivityId, List<school.hei.federationagricole.entity.dto.CreateMembershipFee> dtos) {
        String sql = """
            INSERT INTO membership_fee (id_collectivity, label, frequency, amount, eligible_from, is_active)
            VALUES (?, ?, ?::cotisation_frequency, ?, ?, true)
            RETURNING id
            """;
        List<MembershipFee> created = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            for (school.hei.federationagricole.entity.dto.CreateMembershipFee dto : dtos) {
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, collectivityId);
                    stmt.setString(2, dto.getLabel());
                    stmt.setString(3, dto.getFrequency().name());
                    stmt.setBigDecimal(4, dto.getAmount());
                    if (dto.getEligibleFrom() != null) {
                        stmt.setDate(5, java.sql.Date.valueOf(dto.getEligibleFrom()));
                    } else {
                        stmt.setNull(5, java.sql.Types.DATE);
                    }
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        created.add(findById(rs.getInt("id")));
                    }
                }
            }
            connection.commit();
            return created;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new RuntimeException("Failed to save membership fees", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }

    private MembershipFee mapRow(ResultSet rs) throws SQLException {
        boolean active = rs.getBoolean("is_active");
        return MembershipFee.builder()
                .id(rs.getInt("id"))
                .collectivityId(rs.getInt("id_collectivity"))
                .label(rs.getString("label"))
                .frequency(Frequency.valueOf(rs.getString("frequency")))
                .amount(rs.getBigDecimal("amount"))
                .eligibleFrom(rs.getDate("eligible_from") != null
                        ? rs.getDate("eligible_from").toLocalDate() : null)
                .status(active ? ActivityStatus.ACTIVE : ActivityStatus.INACTIVE)
                .build();
    }
}
