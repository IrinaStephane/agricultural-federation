package school.hei.federationagricole.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.federationagricole.entity.ActivityAttendance;
import school.hei.federationagricole.entity.CollectivityActivity;
import school.hei.federationagricole.entity.dto.*;
import school.hei.federationagricole.exception.BadRequestException;
import school.hei.federationagricole.exception.NotFoundException;
import school.hei.federationagricole.repository.ActivityRepository;
import school.hei.federationagricole.repository.CollectivityRepository;
import school.hei.federationagricole.repository.MemberRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;

    // E: GET /collectivities/{id}/activities
    public List<CollectivityActivity> getActivities(String collectivityId) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }
        return activityRepository.findByCollectivityId(collectivityId);
    }

    // E: POST /collectivities/{id}/activities
    public List<CollectivityActivity> addActivities(String collectivityId,
                                                    List<CreateCollectivityActivity> dtos) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }
        for (CreateCollectivityActivity dto : dtos) {
            boolean hasRecurrence = dto.getRecurrenceWeekOrdinal() != null
                    || dto.getRecurrenceDayOfWeek() != null;
            boolean hasExecutiveDate = dto.getExecutiveDate() != null;
            if (hasRecurrence && hasExecutiveDate) {
                throw new BadRequestException(
                        "Cannot provide both recurrence rule and executive date for activity: "
                                + dto.getLabel());
            }
            if (dto.getLabel() == null || dto.getLabel().isBlank()) {
                throw new BadRequestException("Activity label must not be blank.");
            }
            if (dto.getActivityType() == null) {
                throw new BadRequestException("Activity type is required.");
            }
        }
        return activityRepository.saveAll(collectivityId, dtos);
    }

    // F: POST /collectivities/{id}/activities/{activityId}/attendance
    public List<ActivityMemberAttendanceResponse> setAttendance(String collectivityId,
                                                                String activityId,
                                                                List<CreateActivityMemberAttendance> dtos) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }
        CollectivityActivity activity = activityRepository.findActivityById(activityId);
        if (activity == null || !activity.getCollectivityId().equals(collectivityId)) {
            throw new NotFoundException("Activity not found with id " + activityId);
        }
        for (CreateActivityMemberAttendance dto : dtos) {
            if (!memberRepository.existsById(dto.getMemberIdentifier())) {
                throw new NotFoundException("Member not found: " + dto.getMemberIdentifier());
            }
        }
        List<ActivityAttendance> saved = activityRepository.saveAttendance(activityId, dtos);
        return saved.stream().map(this::toResponse).toList();
    }

    // F: GET /collectivities/{id}/activities/{activityId}/attendance
    public List<ActivityMemberAttendanceResponse> getAttendance(String collectivityId, String activityId) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id " + collectivityId);
        }
        CollectivityActivity activity = activityRepository.findActivityById(activityId);
        if (activity == null || !activity.getCollectivityId().equals(collectivityId)) {
            throw new NotFoundException("Activity not found with id " + activityId);
        }
        return activityRepository.findAttendanceByActivityId(activityId)
                .stream().map(this::toResponse).toList();
    }

    private ActivityMemberAttendanceResponse toResponse(ActivityAttendance att) {
        MemberDescription desc = MemberDescription.builder()
                .id(att.getMember().getId())
                .firstName(att.getMember().getFirstName())
                .lastName(att.getMember().getLastName())
                .email(att.getMember().getEmail())
                .build();
        return ActivityMemberAttendanceResponse.builder()
                .id(att.getId())
                .memberDescription(desc)
                .attendanceStatus(att.getAttendanceStatus())
                .build();
    }
}