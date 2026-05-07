package school.hei.federationagricole.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.AttendanceStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAttendance {
    private String id;
    private String activityId;
    private Member member;
    private AttendanceStatus attendanceStatus;
}