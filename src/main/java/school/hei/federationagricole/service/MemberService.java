package school.hei.federationagricole.service;

import org.springframework.stereotype.Service;
import school.hei.federationagricole.controller.dto.CreateMember;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.MemberIdentifier;
import school.hei.federationagricole.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> createMembers(List<CreateMember> createMembers) {
        List<Member> createdMembers = new ArrayList<>();
        for (CreateMember dto : createMembers) {
            validateMember(dto);
            
            Member member = Member.builder()
                    .id(new MemberIdentifier(UUID.randomUUID().toString()))
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .birthDate(dto.getBirthDate())
                    .gender(dto.getGender())
                    .address(dto.getAddress())
                    .profession(dto.getProfession())
                    .phoneNumber(dto.getPhoneNumber())
                    .email(dto.getEmail())
                    .occupation(dto.getOccupation())
                    .registrationFeePaid(dto.isRegistrationFeePaid())
                    .membershipDuesPaid(dto.isMembershipDuesPaid())
                    .build();

            Member saved = memberRepository.save(member);
            if (dto.getReferees() != null && !dto.getReferees().isEmpty()) {
                memberRepository.saveReferees(saved.getId().getValue(), dto.getReferees());
            }
            if (dto.getCollectivityIdentifier() != null) {
                memberRepository.saveCollectivityMembership(saved.getId().getValue(), dto.getCollectivityIdentifier());
            }
            
            createdMembers.add(memberRepository.findById(saved.getId().getValue()).orElseThrow());
        }
        return createdMembers;
    }

    private void validateMember(CreateMember dto) {
        if (!dto.isRegistrationFeePaid() || !dto.isMembershipDuesPaid()) {
            throw new IllegalArgumentException("Registration fee and membership dues must be paid.");
        }
        if (dto.getReferees() != null) {
            for (String refereeId : dto.getReferees()) {
                if (memberRepository.findById(refereeId).isEmpty()) {
                    throw new RuntimeException("Referee not found: " + refereeId);
                }
            }
        }
    }
}
