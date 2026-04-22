package school.hei.federationagricole.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.PaymentMode;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMemberPayment {
    private BigDecimal amount;
    private Integer membershipFeeIdentifier;
    private Integer accountCreditedIdentifier;
    private PaymentMode paymentMode;
}
