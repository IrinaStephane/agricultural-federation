package school.hei.federationagricole.validator;

import org.springframework.stereotype.Component;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.exception.SponsorTenureException;

@Component
public class SponsorTenureValidator {
    public void validate(Member sponsor){
        if(!sponsor.isAValidSponsor()){
            throw new SponsorTenureException(sponsor.getId() + "'s tenure in Federation is below 90 days");
        }
    }
}
