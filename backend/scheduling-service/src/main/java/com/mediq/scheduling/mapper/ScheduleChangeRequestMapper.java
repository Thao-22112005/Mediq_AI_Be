package com.mediq.scheduling.mapper;

import com.mediq.scheduling.dto.response.ScheduleChangeRequestResponse;
import com.mediq.scheduling.entity.ScheduleChangeRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduleChangeRequestMapper {

    public ScheduleChangeRequestResponse toResponse(ScheduleChangeRequest entity) {
        if (entity == null) {
            return null;
        }
        return new ScheduleChangeRequestResponse(
                entity.getRequestId(),
                entity.getSchedule() != null ? entity.getSchedule().getScheduleId() : null,
                entity.getDoctorId(),
                entity.getRequestedStartTime(),
                entity.getRequestedEndTime(),
                entity.getRequestedRoomId(),
                entity.getReason(),
                entity.getStatus(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<ScheduleChangeRequestResponse> toResponseList(List<ScheduleChangeRequest> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
