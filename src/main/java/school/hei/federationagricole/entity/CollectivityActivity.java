package school.hei.federationagricole.entity;

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
public class CollectivityActivity {
    private String id;
    private String collectivityId;
    private String label;
    private ActivityType activityType;
    private List<CollectivityOccupation> memberOccupationConcerned;
    // For one-shot activities
    private LocalDate executiveDate;
    // For recurring activities
    private Integer recurrenceWeekOrdinal;
    private WeekDay recurrenceDayOfWeek;
}