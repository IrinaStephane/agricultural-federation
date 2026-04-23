package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.account.FinancialAccount;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.AccountRepository;
import school.hei.federationagricole.repository.CollectivityRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class FinancialAccountService {

    private final AccountRepository      accountRepository;
    private final CollectivityRepository collectivityRepository;

    public List<FinancialAccount> getByCollectivity(Integer collectivityId, LocalDate at) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }
        if (at != null) {
            return accountRepository.findByCollectivityIdAtDate(collectivityId, at);
        }
        return accountRepository.findByCollectivityId(collectivityId);
    }
}