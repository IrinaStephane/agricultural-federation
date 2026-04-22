package school.hei.federationagricole.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.CollectivityOccupation;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberCollectivity {
    private int id;
    private Member member;
    private Collectivity collectivity;
    private CollectivityOccupation occupation;
    private Instant startDate;
    private Instant endDate;
}
