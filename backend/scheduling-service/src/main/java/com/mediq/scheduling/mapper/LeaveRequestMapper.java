package com.mediq.scheduling.mapper;

import com.mediq.scheduling.dto.response.LeaveRequestResponse;
import com.mediq.scheduling.entity.LeaveRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaveRequestMapper {

    public LeaveRequestResponse toResponse(LeaveRequest entity) {
        if (entity == null) {
            return null;
        }
        return new LeaveRequestResponse(
                entity.getLeaveRequestId(),
                entity.getDoctorId(),
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getReason(),
                entity.getStatus(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<LeaveRequestResponse> toResponseList(List<LeaveRequest> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
