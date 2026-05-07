package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.dto.CollectivityLocalStatistics;
import school.hei.federationagricole.entity.dto.CollectivityOverallStatistics;
import school.hei.federationagricole.entity.dto.MemberDescription;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.ActivityRepository;
import school.hei.federationagricole.repository.CollectivityRepository;
import school.hei.federationagricole.repository.StatisticsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final CollectivityRepository collectivityRepository;
    private final ActivityRepository activityRepository;

    // G: GET /collectivites/{id}/statistics
    public List<CollectivityLocalStatistics> getLocalStatistics(String collectivityId,
                                                                LocalDate from, LocalDate to) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }

        Map<String, Object[]> membersInfo = statisticsRepository.findMembersInfoByCollectivity(collectivityId);
        Map<String, BigDecimal> earnedMap = statisticsRepository.getEarnedAmountByMember(collectivityId, from, to);
        Map<String, BigDecimal> unpaidMap = statisticsRepository.getUnpaidAmountByMember(collectivityId, from, to);

        List<CollectivityLocalStatistics> result = new ArrayList<>();
        for (Map.Entry<String, Object[]> entry : membersInfo.entrySet()) {
            String memberId = entry.getKey();
            Object[] info = entry.getValue();

            MemberDescription desc = MemberDescription.builder()
                    .id(memberId)
                    .firstName((String) info[0])
                    .lastName((String) info[1])
                    .email((String) info[2])
                    .occupation((String) info[3])
                    .build();

            BigDecimal earned = earnedMap.getOrDefault(memberId, BigDecimal.ZERO);
            BigDecimal unpaid = unpaidMap.getOrDefault(memberId, BigDecimal.ZERO);

            // Bonus 2: attendance rate
            Double attendanceRate;
            try {
                attendanceRate = activityRepository.getMemberAttendanceRate(memberId, collectivityId, from, to);
            } catch (Exception e) {
                attendanceRate = null;
            }

            result.add(CollectivityLocalStatistics.builder()
                    .memberDescription(desc)
                    .earnedAmount(earned)
                    .unpaidAmount(unpaid.max(BigDecimal.ZERO))
                    .attendanceRate(attendanceRate)
                    .build());
        }
        return result;
    }

    // H: GET /collectivites/statistics
    public List<CollectivityOverallStatistics> getOverallStatistics(LocalDate from, LocalDate to) {
        List<String> collectivityIds = statisticsRepository.findAllCollectivityIds();
        Map<String, Integer> newMembersMap = statisticsRepository.getNewMemberCountByCollectivity(from, to);
        Map<String, Double> duePercentMap = statisticsRepository.getMemberCurrentDuePercentageByCollectivity(from, to);

        List<CollectivityOverallStatistics> result = new ArrayList<>();
        for (String colId : collectivityIds) {
            Object[] info = statisticsRepository.findCollectivityInfo(colId);
            if (info == null) continue;

            CollectivityOverallStatistics.CollectivityInfo colInfo =
                    CollectivityOverallStatistics.CollectivityInfo.builder()
                            .id((String) info[0])
                            .name((String) info[1])
                            .number((String) info[2])
                            .build();

            // Bonus 2: overall attendance rate
            Double attendanceRate;
            try {
                attendanceRate = activityRepository.getCollectivityAttendanceRate(colId, from, to);
            } catch (Exception e) {
                attendanceRate = null;
            }

            result.add(CollectivityOverallStatistics.builder()
                    .collectivityInformation(colInfo)
                    .newMembersNumber(newMembersMap.getOrDefault(colId, 0))
                    .overallMemberCurrentDuePercentage(duePercentMap.getOrDefault(colId, 100.0))
                    .overallAttendanceRate(attendanceRate)
                    .build());
        }
        return result;
    }
}