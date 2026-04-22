package school.hei.federationagricole.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.entity.dto.CreateCollectivity;
import school.hei.federationagricole.entity.dto.CreateStructure;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class CollectivityValidator {

    private final MemberRepository memberRepository;

    public void validateCollectivityCreation(CreateCollectivity req) {

        if (!req.isFederationApproval()) {
            throw new BadRequestException("Collectivity must have federation approval.");
        }
        if (req.getLocation() == null || req.getLocation().isBlank()) {
            throw new BadRequestException("Collectivity must have a location.");
        }
        if (req.getStructure() == null) {
            throw new BadRequestException("Collectivity structure is required.");
        }

        List<Integer> memberIds = req.getMemberIds();
        if (memberIds == null || memberIds.isEmpty()) {
            throw new BadRequestException("Collectivity must have at least 10 members.");
        }

        validateAllMembersExist(memberIds);

        List<Member> members = memberRepository.findByIds(memberIds);

        if (members.size() < 10) {
            throw new BadRequestException(
                    "Collectivity must have at least 10 members (currently %d).".formatted(members.size()));
        }

        long withSeniority = members.stream().filter(Member::isAValidSponsor).count();
        if (withSeniority < 5) {
            throw new BadRequestException(
                    "Collectivity needs at least 5 members with 6+ months seniority (currently %d)."
                            .formatted(withSeniority));
        }

        validateStructure(req.getStructure(), memberIds);
    }

    private void validateAllMembersExist(List<Integer> memberIds) {
        List<Integer> missing = new ArrayList<>();
        for (Integer id : memberIds) {
            if (!memberRepository.existsById(id)) missing.add(id);
        }
        if (!missing.isEmpty()) {
            throw new NotFoundException("Members not found with IDs: " + missing);
        }
    }

    private void validateStructure(CreateStructure structure, List<Integer> memberIds) {
        if (structure.getPresidentId()      == null) throw new BadRequestException("President ID is required.");
        if (structure.getVicePresidentId()  == null) throw new BadRequestException("Vice President ID is required.");
        if (structure.getTreasurerId()      == null) throw new BadRequestException("Treasurer ID is required.");
        if (structure.getSecretaryId()      == null) throw new BadRequestException("Secretary ID is required.");

        // BUG FIX: condition was inverted — should throw when member does NOT exist
        checkExists(structure.getPresidentId(),     "President");
        checkExists(structure.getVicePresidentId(), "Vice President");
        checkExists(structure.getTreasurerId(),     "Treasurer");
        checkExists(structure.getSecretaryId(),     "Secretary");

        checkInList(structure.getPresidentId(),     memberIds, "President");
        checkInList(structure.getVicePresidentId(), memberIds, "Vice President");
        checkInList(structure.getTreasurerId(),     memberIds, "Treasurer");
        checkInList(structure.getSecretaryId(),     memberIds, "Secretary");

        List<Integer> roleIds = List.of(
                structure.getPresidentId(), structure.getVicePresidentId(),
                structure.getTreasurerId(), structure.getSecretaryId());
        if (roleIds.stream().distinct().count() != 4) {
            throw new BadRequestException("Each specific post must be held by a different member.");
        }
    }

    /** BUG FIX: throw NotFoundException when member does NOT exist (was inverted before). */
    private void checkExists(Integer memberId, String role) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException(role + " not found with ID: " + memberId);
        }
    }

    private void checkInList(Integer memberId, List<Integer> memberIds, String role) {
        if (!memberIds.contains(memberId)) {
            throw new BadRequestException(role + " must be included in the collectivity member list.");
        }
    }
}
