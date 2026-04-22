package school.hei.federationagricole.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStructure {
    @JsonProperty("president")
    private Integer presidentId;

    @JsonProperty("vicePresident")
    private Integer vicePresidentId;

    @JsonProperty("treasurer")
    private Integer treasurerId;

    @JsonProperty("secretary")
    private Integer secretaryId;
}