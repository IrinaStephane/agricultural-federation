package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.MembershipFee;
import school.hei.federationagricole.entity.dto.*;
import school.hei.federationagricole.entity.enums.Frequency;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.CollectivityRepository;
import school.hei.federationagricole.repository.StatisticsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final CollectivityRepository collectivityRepository;

    // -----------------------------------------------------------------------
    //  G – Local statistics: GET /collectivites/{id}/statistics
    // -----------------------------------------------------------------------

    /**
     * Returns, for each active member of the collectivity, the amount they paid
     * during [from, to] (earnedAmount) and the potential unpaid amount computed
     * from all ACTIVE membership fees during that same period (unpaidAmount).
     */
    public List<CollectivityLocalStatistics> getLocalStatistics(Integer collectivityId,
                                                                LocalDate from,
                                                                LocalDate to) {
        // 1. Verify collectivity exists
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }

        // 2. Fetch all active membership fees (only ACTIVE ones count for unpaid calc)
        List<MembershipFee> activeFees =
                statisticsRepository.findActiveFeesByCollectivity(collectivityId);

        // 3. Compute total expected amount from active fees over [from, to]
        BigDecimal totalExpectedFromActiveFees = activeFees.stream()
                .map(fee -> computeExpectedAmount(fee, from, to))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Build per-member statistics
        List<Map<String, Object>> members =
                statisticsRepository.findActiveMembersWithOccupation(collectivityId);

        List<CollectivityLocalStatistics> result = new ArrayList<>();
        for (Map<String, Object> row : members) {
            Integer memberId = (Integer) row.get("id");

            // Earned = total payments by this member to this collectivity in [from, to]
            BigDecimal earned = statisticsRepository.findEarnedAmountByMember(
                    memberId, collectivityId, from, to);

            // Unpaid = sum of (expected per fee – what they paid toward that fee), floored at 0
            BigDecimal unpaid = BigDecimal.ZERO;
            for (MembershipFee fee : activeFees) {
                BigDecimal expectedForFee = computeExpectedAmount(fee, from, to);
                BigDecimal paidForFee = statisticsRepository.findPaidAmountByMemberAndFee(
                        memberId, fee.getId(), from, to);
                BigDecimal diff = expectedForFee.subtract(paidForFee);
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    unpaid = unpaid.add(diff);
                }
            }

            MemberDescription desc = MemberDescription.builder()
                    .id(String.valueOf(memberId))
                    .firstName((String) row.get("firstName"))
                    .lastName((String) row.get("lastName"))
                    .email((String) row.get("email"))
                    .occupation((String) row.get("occupation"))
                    .build();

            result.add(CollectivityLocalStatistics.builder()
                    .memberDescription(desc)
                    .earnedAmount(earned)
                    .unpaidAmount(unpaid)
                    .build());
        }

        return result;
    }

    // -----------------------------------------------------------------------
    //  H – Overall statistics: GET /collectivities/statistics
    // -----------------------------------------------------------------------

    /**
     * For each collectivity, returns:
     * - newMembersNumber: count of members who joined during [from, to]
     * - overallMemberCurrentDuePercentage: % of active members who have paid
     *   at least the expected amount from all ACTIVE fees during [from, to]
     */
    public List<CollectivityOverallStatistics> getOverallStatistics(LocalDate from,
                                                                    LocalDate to) {
        List<Map<String, Object>> collectivities =
                statisticsRepository.findAllCollectivities();

        List<CollectivityOverallStatistics> result = new ArrayList<>();

        for (Map<String, Object> col : collectivities) {
            Integer collectivityId = (Integer) col.get("id");

            // New members in period
            int newMembers = statisticsRepository.countNewMembers(collectivityId, from, to);

            // Active members count
            List<Integer> activeMemberIds =
                    statisticsRepository.findActiveMemberIds(collectivityId);
            int totalActive = activeMemberIds.size();

            // Active membership fees for this collectivity
            List<MembershipFee> activeFees =
                    statisticsRepository.findActiveFeesByCollectivity(collectivityId);

            // Count members who are current with all active dues in the period
            int currentCount = 0;
            if (totalActive > 0) {
                for (Integer memberId : activeMemberIds) {
                    boolean isCurrent = isMemberCurrentWithDues(
                            memberId, activeFees, from, to);
                    if (isCurrent) currentCount++;
                }
            }

            // Compute percentage (0 if no active members)
            BigDecimal percentage = totalActive == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(currentCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalActive), 2, RoundingMode.HALF_UP);

            CollectivityInformationResponse info = CollectivityInformationResponse.builder()
                    .name((String) col.get("name"))
                    .number((String) col.get("number"))
                    .build();

            result.add(CollectivityOverallStatistics.builder()
                    .collectivityInformation(info)
                    .newMembersNumber(newMembers)
                    .overallMemberCurrentDuePercentage(percentage)
                    .build());
        }

        return result;
    }

    // -----------------------------------------------------------------------
    //  Helpers
    // -----------------------------------------------------------------------

    /**
     * A member is "current with dues" when, for every ACTIVE membership fee,
     * the amount they paid toward that specific fee during [from, to] is >= the
     * expected amount for that fee during the period.
     * If there are no active fees, the member is considered current.
     */
    private boolean isMemberCurrentWithDues(Integer memberId,
                                            List<MembershipFee> activeFees,
                                            LocalDate from, LocalDate to) {
        for (MembershipFee fee : activeFees) {
            BigDecimal expected = computeExpectedAmount(fee, from, to);
            if (expected.compareTo(BigDecimal.ZERO) <= 0) continue; // fee not applicable in period

            BigDecimal paid = statisticsRepository.findPaidAmountByMemberAndFee(
                    memberId, fee.getId(), from, to);

            if (paid.compareTo(expected) < 0) {
                return false; // not up to date on this fee
            }
        }
        return true;
    }

    /**
     * Computes the total expected amount from a membership fee during [from, to].
     *
     * <p>Calculation rules:
     * <ul>
     *   <li>MONTHLY  : number of calendar months in [effectiveStart, to] × amount</li>
     *   <li>ANNUALLY : number of full years  in [effectiveStart, to] × amount (≥ 1 if applicable)</li>
     *   <li>WEEKLY   : number of weeks       in [effectiveStart, to] × amount</li>
     *   <li>PUNCTUALLY: amount once, if eligible_from is on or before `to`</li>
     * </ul>
     * The effective start is max(eligibleFrom, from).
     */
    private BigDecimal computeExpectedAmount(MembershipFee fee, LocalDate from, LocalDate to) {
        LocalDate eligibleFrom = fee.getEligibleFrom();

        // If fee has no start date or its start date is after `to`, it's not applicable
        if (eligibleFrom != null && eligibleFrom.isAfter(to)) {
            return BigDecimal.ZERO;
        }

        // Effective start of the period for this fee
        LocalDate effectiveStart = (eligibleFrom != null && eligibleFrom.isAfter(from))
                ? eligibleFrom : from;

        if (effectiveStart.isAfter(to)) {
            return BigDecimal.ZERO;
        }

        BigDecimal amount = fee.getAmount();
        Frequency frequency = fee.getFrequency();

        return switch (frequency) {
            case MONTHLY -> {
                // Count months from start of effectiveStart's month to start of to's month (inclusive)
                long months = ChronoUnit.MONTHS.between(
                        effectiveStart.withDayOfMonth(1),
                        to.withDayOfMonth(1)) + 1L;
                yield amount.multiply(BigDecimal.valueOf(Math.max(0, months)));
            }
            case ANNUALLY -> {
                // At least 1 if the fee is applicable in the period
                long years = ChronoUnit.YEARS.between(effectiveStart, to.plusDays(1));
                yield amount.multiply(BigDecimal.valueOf(Math.max(1, years)));
            }
            case WEEKLY -> {
                long weeks = ChronoUnit.WEEKS.between(effectiveStart, to.plusDays(1));
                yield amount.multiply(BigDecimal.valueOf(Math.max(0, weeks)));
            }
            case PUNCTUALLY ->
                // One-time fee: applies once (eligible_from must be <= to, already checked above)
                    amount;
        };
    }
}