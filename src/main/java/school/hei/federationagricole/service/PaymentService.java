package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.MemberPayment;
import school.hei.federationagricole.entity.MembershipFee;
import school.hei.federationagricole.entity.dto.CreateMemberPayment;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.AccountRepository;
import school.hei.federationagricole.repository.MemberRepository;
import school.hei.federationagricole.repository.MembershipFeeRepository;
import school.hei.federationagricole.repository.TransactionRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final Connection               connection;
    private final TransactionRepository    transactionRepository;
    private final MemberRepository         memberRepository;
    private final MembershipFeeRepository  membershipFeeRepository;
    private final AccountRepository        accountRepository;

    public List<MemberPayment> createPayments(Integer memberId, List<CreateMemberPayment> dtos) {

        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException("Member not found with id " + memberId);
        }

        for (CreateMemberPayment dto : dtos) {
            if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Payment amount must be greater than 0.");
            }
            if (dto.getPaymentMode() == null) {
                throw new BadRequestException("Payment mode is required.");
            }
            if (dto.getAccountCreditedIdentifier() == null) {
                throw new BadRequestException("Account identifier is required.");
            }
            if (!accountRepository.existsById(dto.getAccountCreditedIdentifier())) {
                throw new NotFoundException("Account not found with id " + dto.getAccountCreditedIdentifier());
            }
            if (dto.getMembershipFeeIdentifier() != null
                    && !membershipFeeRepository.existsById(dto.getMembershipFeeIdentifier())) {
                throw new NotFoundException("Membership fee not found with id "
                        + dto.getMembershipFeeIdentifier());
            }
        }

        return dtos.stream()
                .map(dto -> {
                    Integer collectivityId = resolveCollectivityId(
                            dto.getMembershipFeeIdentifier(),
                            dto.getAccountCreditedIdentifier());
                    return transactionRepository.savePayment(
                            memberId,
                            collectivityId,
                            dto.getMembershipFeeIdentifier(),
                            dto.getAccountCreditedIdentifier(),
                            dto.getAmount(),
                            dto.getPaymentMode());
                })
                .toList();
    }

    private Integer resolveCollectivityId(Integer membershipFeeId, Integer accountId) {
        if (membershipFeeId != null) {
            MembershipFee fee = membershipFeeRepository.findById(membershipFeeId);
            if (fee != null) return fee.getCollectivityId();
        }
        String sql = "SELECT id_collectivity FROM account WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_collectivity");
            throw new NotFoundException("Cannot resolve collectivity for account " + accountId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to resolve collectivity from account", e);
        }
    }
}
