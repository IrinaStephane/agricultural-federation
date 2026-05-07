package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.CollectivityTransaction;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.MemberPayment;
import school.hei.federationagricole.entity.account.FinancialAccount;
import school.hei.federationagricole.entity.enums.Gender;
import school.hei.federationagricole.entity.enums.PaymentMode;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class TransactionRepository {

    private final Connection connection;
    private final AccountRepository accountRepository;

    public List<CollectivityTransaction> findByCollectivityIdAndPeriod(
            String collectivityId, LocalDate from, LocalDate to) {

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
            stmt.setString(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FinancialAccount account = accountRepository.findById(rs.getString("id_account"));
                result.add(CollectivityTransaction.builder()
                        .id(rs.getString("id"))
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

    public MemberPayment savePayment(String memberId, String collectivityId,
                                     String membershipFeeId, String accountId,
                                     BigDecimal amount, PaymentMode paymentMode) {

        // Générer un ID string unique pour la transaction
        String transactionId = "tx-" + UUID.randomUUID().toString().substring(0, 8);

        String insertSql = """
            INSERT INTO transaction
                (id, id_member, id_collectivity, id_membership_fee, id_account,
                 transaction_type, amount, transaction_date, payment_mode)
            VALUES (?, ?, ?, ?, ?, 'IN'::transaction_type, ?, NOW(), ?::payment_mode)
            RETURNING transaction_date
            """;

        try {
            connection.setAutoCommit(false);

            LocalDate creationDate;

            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                stmt.setString(1, transactionId);
                stmt.setString(2, memberId);
                stmt.setString(3, collectivityId);
                if (membershipFeeId != null) stmt.setString(4, membershipFeeId);
                else stmt.setNull(4, Types.VARCHAR);
                stmt.setString(5, accountId);
                stmt.setBigDecimal(6, amount);
                stmt.setString(7, paymentMode.name());

                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) throw new RuntimeException("Insert transaction returned no row");
                creationDate = rs.getTimestamp("transaction_date").toLocalDateTime().toLocalDate();
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