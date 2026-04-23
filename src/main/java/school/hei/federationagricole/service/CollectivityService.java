package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.Collectivity;
import school.hei.federationagricole.entity.dto.CollectivityIdentificationRequest;
import school.hei.federationagricole.entity.dto.CollectivityResponse;
import school.hei.federationagricole.entity.dto.CreateCollectivity;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.CollectivityAlreadyIdentifiedException;
import school.hei.federationagricole.exception.NotFoundException;
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

    //  Feature A: create collectivities

    public List<CollectivityResponse> createCollectivities(List<CreateCollectivity> createCollectivities) {
        List<Collectivity> collectivitiesToSave = new ArrayList<>();
        List<List<Integer>> memberIdsList      = new ArrayList<>();
        List<Integer> presidentIds             = new ArrayList<>();
        List<Integer> vicePresidentIds         = new ArrayList<>();
        List<Integer> treasurerIds             = new ArrayList<>();
        List<Integer> secretaryIds             = new ArrayList<>();

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

        List<Collectivity> saved = repository.saveAll(
                collectivitiesToSave, memberIdsList,
                presidentIds, vicePresidentIds, treasurerIds, secretaryIds);

        return saved.stream().map(this::buildResponse).toList();
    }

    //  Feature A complement: get collectivity by id
    public CollectivityResponse getById(Integer id) {
        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id " + id);
        }
        return buildResponse(collectivity);
    }

    //  Feature J: attribute a unique number and name to a collectivity
    public CollectivityResponse identifyCollectivity(Integer id,
                                                     CollectivityIdentificationRequest request) {

        // 1. Check collectivity exists
        Collectivity existing = repository.findById(id);
        if (existing == null) {
            throw new NotFoundException("Collectivity with id " + id + " not found.");
        }

        // 2. Validate request fields are not blank
        if (request.getNumber() == null || request.getNumber().isBlank()) {
            throw new BadRequestException("Number must not be blank.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Name must not be blank.");
        }

        // 3. Immutability check: number cannot be changed once assigned
        if (existing.getNumber() != null) {
            throw new CollectivityAlreadyIdentifiedException(
                    "Collectivity number has already been assigned and cannot be changed.");
        }

        // 4. Immutability check: name cannot be changed once assigned
        if (existing.getName() != null) {
            throw new CollectivityAlreadyIdentifiedException(
                    "Collectivity name has already been assigned and cannot be changed.");
        }

        // 5. Uniqueness check: number must not already exist in another collectivity
        if (repository.existsByNumber(request.getNumber())) {
            throw new BadRequestException(
                    "A collectivity with number '" + request.getNumber() + "' already exists.");
        }

        // 6. Uniqueness check: name must not already exist in another collectivity
        if (repository.existsByName(request.getName())) {
            throw new BadRequestException(
                    "A collectivity with name '" + request.getName() + "' already exists.");
        }

        // 7. Persist
        Collectivity updated = repository.assignIdentification(id, request.getNumber(), request.getName());

        return buildResponse(updated);
    }


    private CollectivityResponse buildResponse(Collectivity collectivity) {
        return CollectivityResponse.builder()
                .id(String.valueOf(collectivity.getId()))
                .number(collectivity.getNumber())
                .name(collectivity.getName())
                .location(collectivity.getLocation())
                .structure(collectivity.getStructure())
                .members(collectivity.getMembers())
                .build();
    }
}