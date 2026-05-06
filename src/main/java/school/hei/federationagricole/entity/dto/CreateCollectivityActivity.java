package school.hei.federationagricole.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.ActivityType;
import school.hei.federationagricole.entity.enums.CollectivityOccupation;
import school.hei.federationagricole.entity.enums.WeekDay;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCollectivityActivity {
    private String label;
    private ActivityType activityType;
    private List<CollectivityOccupation> memberOccupationConcerned;
    // Recurrence rule fields
    private Integer recurrenceWeekOrdinal;
    private WeekDay recurrenceDayOfWeek;
    // One-shot date
    private LocalDate executiveDate;
}