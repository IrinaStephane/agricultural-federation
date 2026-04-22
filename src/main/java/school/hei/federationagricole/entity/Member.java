package school.hei.federationagricole.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.federationagricole.entity.enums.Gender;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Instant enrolmentDate;
    private String address;
    private String email;
    private String phoneNumber;
    private String profession;
    private Gender gender;

    @JsonIgnore
    @Builder.Default
    private List<Member> referees = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    private List<MemberCollectivity> memberCollectivities = new ArrayList<>();

    public boolean isAValidSponsor() {
        return enrolmentDate != null &&
                Duration.between(enrolmentDate, Instant.now()).toDays() >= 90;
    }

    public List<Integer> getIdsOfActualBelongingCollectivities() {
        if (memberCollectivities == null) return List.of();

        return memberCollectivities.stream()
                .filter(mc -> mc.getEndDate() == null)
                .map(mc -> mc.getCollectivity().getId())
                .toList();
    }
    public List<Integer> getReferees(){
        return this.referees.stream().map(Member::getId).toList();
    }


}