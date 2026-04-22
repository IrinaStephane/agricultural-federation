package school.hei.federationagricole.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Federation {
    private Integer id;
    private double cotisationPercentage;
    private Structure structure;
}
