package school.hei.federationagricole.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.ActivityStatus;
import school.hei.federationagricole.entity.enums.Frequency;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipFee {
    private Integer id;
    private Integer collectivityId;
    private String label;
    private Frequency frequency;
    private BigDecimal amount;
    private LocalDate eligibleFrom;
    private ActivityStatus status;
}
