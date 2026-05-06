package school.hei.federationagricole.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectivityOverallStatistics {
    private CollectivityInfo collectivityInformation;
    private Integer newMembersNumber;
    private Double overallMemberCurrentDuePercentage;
    // Bonus 2: overall attendance rate
    private Double overallAttendanceRate;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CollectivityInfo {
        private String id;
        private String name;
        private String number;
    }
}