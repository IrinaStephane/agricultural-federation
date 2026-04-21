package school.hei.federationagricole.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.federationagricole.controller.dto.CreateMember;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.service.MemberService;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<List<Member>> createMembers(@RequestBody List<CreateMember> members) {
        return new ResponseEntity<>(memberService.createMembers(members), HttpStatus.CREATED);
    }
}
