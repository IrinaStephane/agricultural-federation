package school.hei.federationagricole.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectivityLocalStatistics {
    private MemberDescription memberDescription;
    private BigDecimal earnedAmount;
    private BigDecimal unpaidAmount;
    // Bonus 2: attendance rate
    private Double attendanceRate;
}