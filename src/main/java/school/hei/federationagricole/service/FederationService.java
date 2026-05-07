package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.Federation;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.FederationRepository;

@Service
@AllArgsConstructor
public class FederationService {
    private final FederationRepository federationRepository;

    public Federation getFederation() {
        return federationRepository.findFederation()
                .orElseThrow(() -> new NotFoundException("Federation not found"));
    }
}