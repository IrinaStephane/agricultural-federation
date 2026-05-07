package school.hei.federationagricole.validator;

import org.springframework.stereotype.Component;
import school.hei.federationagricole.entity.dto.CreateMember;
import school.hei.federationagricole.exception.InsufficientSponsorCount;

import java.util.List;

@Component
public class SponsorCountValidator {

    public void validate(CreateMember member) {
        if (member.getReferees().size() < 2) {
            throw new InsufficientSponsorCount(
                    member.getFirstName() + " : at least two sponsors required"
            );
        }
    }
    public void validate(List<CreateMember> members) {
        for (CreateMember member : members) {
            validate(member);
        }
    }
}
