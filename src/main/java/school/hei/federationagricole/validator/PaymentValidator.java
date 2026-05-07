package school.hei.federationagricole.validator;

import org.springframework.stereotype.Component;
import school.hei.federationagricole.entity.dto.CreateMember;
import school.hei.federationagricole.exception.PaymentException;

import java.util.List;


@Component
public class PaymentValidator {
    public void validate(CreateMember member) {
        if (!member.isMembershipDuesPaid() || !member.isRegistrationFeePaid()) {
            throw new PaymentException(
                    member.getFirstName() + " : payment not completed"
            );
        }
    }
    public void validate(List<CreateMember> members) {
        for (CreateMember member : members) {
            validate(member);
        }
    }
}
