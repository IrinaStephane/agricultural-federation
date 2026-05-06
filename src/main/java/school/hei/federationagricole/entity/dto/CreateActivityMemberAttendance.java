package school.hei.federationagricole.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.AttendanceStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateActivityMemberAttendance {
    private String memberIdentifier;
    private AttendanceStatus attendanceStatus;
}