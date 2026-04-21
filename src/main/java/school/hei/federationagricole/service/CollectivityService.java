package school.hei.federationagricole.service;

import org.springframework.stereotype.Service;
import school.hei.federationagricole.controller.dto.CreateCollectivity;
import school.hei.federationagricole.controller.dto.CreateCollectivityStructure;
import school.hei.federationagricole.entity.Collectivity;
import school.hei.federationagricole.entity.Member;
import school.hei.federationagricole.repository.CollectivityRepository;
import school.hei.federationagricole.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollectivityService {
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;

    public CollectivityService(CollectivityRepository collectivityRepository, MemberRepository memberRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
    }

    public List<Collectivity> createCollectivities(List<CreateCollectivity> createCollectivities) {
        List<Collectivity> createdCollectivities = new ArrayList<>();
        for (CreateCollectivity dto : createCollectivities) {
            validateCollectivity(dto);
            
            // Map members
            List<Member> members = memberRepository.findAllById(dto.getMembers());
            
            Collectivity collectivity = Collectivity.builder()
                    .location(dto.getLocation())
                    .federationApproval(dto.isFederationApproval())
                    .members(members)
                    .build();

            Collectivity saved = collectivityRepository.save(collectivity, dto.getStructure());
            createdCollectivities.add(saved);
        }
        return createdCollectivities;
    }

    private void validateCollectivity(CreateCollectivity dto) {
        if (!dto.isFederationApproval()) {
            throw new IllegalArgumentException("Collectivity without federation approval.");
        }
        if (dto.getStructure() == null || isStructureIncomplete(dto.getStructure())) {
            throw new IllegalArgumentException("Structure missing or incomplete.");
        }
        
        // Check if all members exist
        for (String memberId : dto.getMembers()) {
            if (memberRepository.findById(memberId).isEmpty()) {
                throw new RuntimeException("Member not found: " + memberId);
            }
        }
        
        // Check structure members
        CreateCollectivityStructure s = dto.getStructure();
        String[] ids = {s.getPresident(), s.getVicePresident(), s.getTreasurer(), s.getSecretary()};
        for (String id : ids) {
            if (memberRepository.findById(id).isEmpty()) {
                throw new RuntimeException("Structure member not found: " + id);
            }
        }
    }

    private boolean isStructureIncomplete(CreateCollectivityStructure structure) {
        return structure.getPresident() == null || structure.getVicePresident() == null ||
                structure.getTreasurer() == null || structure.getSecretary() == null;
    }
}
