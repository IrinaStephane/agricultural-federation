package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.Collectivity;
import school.hei.federationagricole.entity.dto.CollectivityResponse;
import school.hei.federationagricole.entity.dto.CreateCollectivity;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.repository.CollectivityRepository;
import school.hei.federationagricole.validator.CollectivityValidator;

import java.util.ArrayList;
import java.util.List;

import java.time.Instant;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final CollectivityValidator validator;

    public List<CollectivityResponse> createCollectivities(List<CreateCollectivity> createCollectivities) throws BadRequestException {
        List<Collectivity> collectivitiesToSave = new ArrayList<>();
        List<List<Integer>> memberIdsList = new ArrayList<>();
        List<Integer> presidentIds = new ArrayList<>();
        List<Integer> vicePresidentIds = new ArrayList<>();
        List<Integer> treasurerIds = new ArrayList<>();
        List<Integer> secretaryIds = new ArrayList<>();

        for (CreateCollectivity request : createCollectivities) {
            validator.validateCollectivityCreation(request);

            Collectivity collectivity = Collectivity.builder()
                    .speciality("Agriculture")
                    .federationApproval(request.isFederationApproval())
                    .authorizationDate(Instant.now())
                    .location(request.getLocation())
                    .build();

            collectivitiesToSave.add(collectivity);
            memberIdsList.add(request.getMemberIds());
            presidentIds.add(request.getStructure().getPresidentId());
            vicePresidentIds.add(request.getStructure().getVicePresidentId());
            treasurerIds.add(request.getStructure().getTreasurerId());
            secretaryIds.add(request.getStructure().getSecretaryId());
        }

        List<Collectivity> savedCollectivities = repository.saveAll(
                collectivitiesToSave,
                memberIdsList,
                presidentIds,
                vicePresidentIds,
                treasurerIds,
                secretaryIds
        );

        return savedCollectivities.stream()
                .map(this::buildResponse)
                .toList();
    }

    private CollectivityResponse buildResponse(Collectivity collectivity) {
        return CollectivityResponse.builder()
                .id(String.valueOf(collectivity.getId()))
                .location(collectivity.getLocation())
                .structure(collectivity.getStructure())
                .members(collectivity.getMembers())
                .build();
    }

}