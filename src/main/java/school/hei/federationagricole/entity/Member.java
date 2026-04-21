package school.hei.federationagricole.entity;

import java.util.List;

public class Member extends MemberInformation{
    private MemberIdentifier id;
    private List<Member> referees;
}
