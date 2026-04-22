package school.hei.federationagricole.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.Structure;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityResponse {
    private String id;
    private String location;
    private Structure structure;
    private List<Member> members;
}