package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.CollectivityTransaction;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.CollectivityRepository;
import school.hei.federationagricole.repository.TransactionRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository  transactionRepository;
    private final CollectivityRepository collectivityRepository;

    public List<CollectivityTransaction> getTransactions(Integer collectivityId,
                                                         LocalDate from, LocalDate to) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }
        if (from == null || to == null) {
            throw new BadRequestException("Query parameters 'from' and 'to' are required.");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date.");
        }
        return transactionRepository.findByCollectivityIdAndPeriod(collectivityId, from, to);
    }
}
