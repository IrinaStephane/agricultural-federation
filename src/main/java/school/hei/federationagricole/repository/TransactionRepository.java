package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.CollectivityTransaction;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.MemberPayment;
import school.hei.federationagricole.entity.account.FinancialAccount;
import school.hei.federationagricole.entity.enums.Gender;
import school.hei.federationagricole.entity.enums.PaymentMode;
import school.hei.federationagricole.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class TransactionRepository {

    private final Connection connection;
    private final AccountRepository accountRepository;

    public List<CollectivityTransaction> findByCollectivityIdAndPeriod(
            Integer collectivityId, LocalDate from, LocalDate to) {

        String sql = """
            SELECT t.id, t.amount, t.transaction_date, t.payment_mode, t.id_account,
                   m.id AS m_id, m.first_name, m.last_name, m.birth_date, m.enrolment_date,
                   m.address, m.email, m.phone_number, m.profession, m.gender
            FROM transaction t
            JOIN member m ON t.id_member = m.id
            WHERE t.id_collectivity = ?
              AND DATE(t.transaction_date) BETWEEN ? AND ?
            ORDER BY t.transaction_date DESC
            """;

        List<CollectivityTransaction> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FinancialAccount account = accountRepository.findById(rs.getInt("id_account"));
                result.add(CollectivityTransaction.builder()
                        .id(rs.getInt("id"))
                        .creationDate(rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate())
                        .amount(rs.getBigDecimal("amount"))
                        .paymentMode(rs.getString("payment_mode") != null
                                ? PaymentMode.valueOf(rs.getString("payment_mode")) : null)
                        .accountCredited(account)
                        .memberDebited(mapMember(rs))
                        .build());
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query transactions", e);
        }
    }

    public MemberPayment savePayment(Integer memberId, Integer collectivityId,
                                     Integer membershipFeeId, Integer accountId,
                                     BigDecimal amount, PaymentMode paymentMode) {
        String insertSql = """
            INSERT INTO transaction
                (id_member, id_collectivity, id_membership_fee, id_account,
                 transaction_type, amount, transaction_date, payment_mode)
            VALUES (?, ?, ?, ?, 'IN'::transaction_type, ?, NOW(), ?::payment_mode)
            RETURNING id, transaction_date
            """;

        try {
            connection.setAutoCommit(false);

            int transactionId;
            LocalDate creationDate;

            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                stmt.setInt(1, memberId);
                stmt.setInt(2, collectivityId);
                if (membershipFeeId != null) stmt.setInt(3, membershipFeeId);
                else stmt.setNull(3, Types.INTEGER);
                stmt.setInt(4, accountId);
                stmt.setBigDecimal(5, amount);
                stmt.setString(6, paymentMode.name());

                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) throw new RuntimeException("Insert transaction returned no row");
                transactionId = rs.getInt("id");
                creationDate  = rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate();
            }

            // Update account balance
            accountRepository.addToBalance(accountId, amount);

            connection.commit();

            FinancialAccount account = accountRepository.findById(accountId);
            return MemberPayment.builder()
                    .id(transactionId)
                    .amount(amount)
                    .paymentMode(paymentMode)
                    .accountCredited(account)
                    .creationDate(creationDate)
                    .build();

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignore */ }
            throw new RuntimeException("Failed to save payment", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { /* ignore */ }
        }
    }


    private Member mapMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getInt("m_id"))
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
