package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.MembershipFee;
import school.hei.federationagricole.entity.dto.CreateMembershipFee;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.CollectivityRepository;
import school.hei.federationagricole.repository.MembershipFeeRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class MembershipFeeService {

    private final MembershipFeeRepository membershipFeeRepository;
    private final CollectivityRepository  collectivityRepository;

    public List<MembershipFee> getByCollectivity(Integer collectivityId) {
        ensureCollectivityExists(collectivityId);
        return membershipFeeRepository.findByCollectivityId(collectivityId);
    }

    public List<MembershipFee> create(Integer collectivityId, List<CreateMembershipFee> dtos) {
        ensureCollectivityExists(collectivityId);

        for (CreateMembershipFee dto : dtos) {
            if (dto.getFrequency() == null) {
                throw new BadRequestException("Frequency is required and must be one of WEEKLY, MONTHLY, ANNUALLY, PUNCTUALLY.");
            }
            if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Amount must be greater than 0.");
            }
            if (dto.getLabel() == null || dto.getLabel().isBlank()) {
                throw new BadRequestException("Label is required.");
            }
        }

        return membershipFeeRepository.saveAll(collectivityId, dtos);
    }

    private void ensureCollectivityExists(Integer id) {
        if (collectivityRepository.findById(id) == null) {
            throw new NotFoundException("Collectivity not found with id " + id);
        }
    }
}