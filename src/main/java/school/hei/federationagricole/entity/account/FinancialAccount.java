package school.hei.federationagricole.entity.account;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CashAccount.class,          name = "CASH"),
        @JsonSubTypes.Type(value = BankAccount.class,          name = "BANK"),
        @JsonSubTypes.Type(value = MobileBankingAccount.class, name = "MOBILE_BANKING")
})
public abstract class FinancialAccount {
    private Integer id;
    private BigDecimal amount;
}