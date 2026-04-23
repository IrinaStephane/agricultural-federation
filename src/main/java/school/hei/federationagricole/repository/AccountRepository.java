package school.hei.federationagricole.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import school.hei.federationagricole.entity.account.*;
import school.hei.federationagricole.entity.enums.Bank;
import school.hei.federationagricole.entity.enums.MobileBankingService;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class AccountRepository {

    private final Connection connection;

    public FinancialAccount findById(Integer accountId) {
        String baseSql = "SELECT id, balance FROM account WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(baseSql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            BigDecimal balance = rs.getBigDecimal("balance");

            return detectType(accountId, balance);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by id", e);
        }
    }

    public boolean existsById(Integer accountId) {
        String sql = "SELECT 1 FROM account WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check account existence", e);
        }
    }

    public List<FinancialAccount> findByCollectivityId(Integer collectivityId) {
        String sql = "SELECT id, balance FROM account WHERE id_collectivity = ?";
        List<FinancialAccount> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FinancialAccount fa = detectType(rs.getInt("id"), rs.getBigDecimal("balance"));
                if (fa != null) result.add(fa);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list accounts for collectivity", e);
        }
    }

    public List<FinancialAccount> findByCollectivityIdAtDate(Integer collectivityId, java.time.LocalDate at) {
        String sql = """
            SELECT a.id,
                   a.balance
                   - COALESCE(SUM(CASE WHEN t.transaction_type = 'IN'
                                        AND DATE(t.transaction_date) > ?
                                   THEN t.amount ELSE 0 END), 0)
                   + COALESCE(SUM(CASE WHEN t.transaction_type = 'OUT'
                                        AND DATE(t.transaction_date) > ?
                                   THEN t.amount ELSE 0 END), 0) AS balance_at
            FROM account a
            LEFT JOIN transaction t ON t.id_account = a.id
            WHERE a.id_collectivity = ?
            GROUP BY a.id, a.balance
            """;
        List<FinancialAccount> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(at));
            stmt.setDate(2, java.sql.Date.valueOf(at));
            stmt.setInt(3, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FinancialAccount fa = detectType(rs.getInt("id"), rs.getBigDecimal("balance_at"));
                if (fa != null) result.add(fa);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list accounts at date for collectivity", e);
        }
    }

    public void addToBalance(Integer accountId, BigDecimal delta) {
        String sql = "UPDATE account SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, delta);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account balance", e);
        }
    }

    private FinancialAccount detectType(Integer accountId, BigDecimal balance) throws SQLException {
        FinancialAccount cash = tryMapCash(accountId, balance);
        if (cash != null) return cash;

        FinancialAccount bank = tryMapBank(accountId, balance);
        if (bank != null) return bank;

        return tryMapMobile(accountId, balance);
    }

    private CashAccount tryMapCash(Integer accountId, BigDecimal balance) throws SQLException {
        String sql = "SELECT id FROM cash_account WHERE id_account = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return CashAccount.builder()
                    .id(accountId)
                    .amount(balance)
                    .build();
        }
    }

    private BankAccount tryMapBank(Integer accountId, BigDecimal balance) throws SQLException {
        String sql = """
            SELECT holder_name, bank_name, bank_code, branch_code, account_number, rib_key
            FROM bank_account WHERE id_account = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return BankAccount.builder()
                    .id(accountId)
                    .amount(balance)
                    .holderName(rs.getString("holder_name"))
                    .bankName(Bank.valueOf(rs.getString("bank_name")))
                    .bankCode(rs.getString("bank_code"))
                    .bankBranchCode(rs.getString("branch_code"))
                    .bankAccountNumber(rs.getString("account_number"))
                    .bankAccountKey(rs.getString("rib_key"))
                    .build();
        }
    }

    private MobileBankingAccount tryMapMobile(Integer accountId, BigDecimal balance) throws SQLException {
        String sql = "SELECT holder_name, service_name, phone_number FROM mobile_money_account WHERE id_account = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            return MobileBankingAccount.builder()
                    .id(accountId)
                    .amount(balance)
                    .holderName(rs.getString("holder_name"))
                    .mobileBankingService(MobileBankingService.valueOf(rs.getString("service_name")))
                    .mobileNumber(rs.getString("phone_number"))
                    .build();
        }
    }
}