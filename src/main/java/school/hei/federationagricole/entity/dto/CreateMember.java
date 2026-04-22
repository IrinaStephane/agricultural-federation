package school.hei.federationagricole.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.CollectivityOccupation;
import school.hei.federationagricole.entity.enums.Gender;

import java.time.LocalDate;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateMember {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String profession;
    private String phoneNumber;
    private String email;

    private CollectivityOccupation occupation;

    private Integer collectivityIdentifier;
    private List<Integer> referees;

    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
}