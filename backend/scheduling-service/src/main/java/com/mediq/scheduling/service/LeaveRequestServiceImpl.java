package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateLeaveRequest;
import com.mediq.scheduling.dto.request.ReviewLeaveRequest;
import com.mediq.scheduling.dto.response.AffectedScheduleDto;
import com.mediq.scheduling.dto.response.ApprovedLeaveResult;
import com.mediq.scheduling.dto.response.LeaveRequestResponse;
import com.mediq.scheduling.entity.LeaveRequest;
import com.mediq.scheduling.entity.LeaveRequestStatus;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.event.EventEnvelope;
import com.mediq.scheduling.event.EventPublisher;
import com.mediq.scheduling.event.payload.LeaveRequestApprovedEventPayload;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.LeaveRequestMapper;
import com.mediq.scheduling.repository.LeaveRequestRepository;
import com.mediq.scheduling.repository.SlotRepository;
import com.mediq.scheduling.repository.WorkScheduleRepository;
import com.mediq.scheduling.validation.LeaveRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestValidator leaveRequestValidator;
    private final LeaveRequestMapper leaveRequestMapper;
    private final WorkScheduleRepository workScheduleRepository;
    private final SlotRepository slotRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public LeaveRequestResponse createLeaveRequest(CreateLeaveRequest request) {
        leaveRequestValidator.validateCreate(request);

        boolean hasOverlap = leaveRequestRepository.hasOverlappingLeave(
                request.getDoctorId(),
                request.getStartDateTime(),
                request.getEndDateTime()
        );

        if (hasOverlap) {
            throw new BusinessException(ErrorCode.LEAVE_REQUEST_OVERLAP,
                    "Doctor already has an overlapping leave request in this time window");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .doctorId(request.getDoctorId())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .reason(request.getReason())
                .status(LeaveRequestStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return leaveRequestMapper.toResponse(saved);
    }

    @Override
    public LeaveRequestResponse getLeaveRequestById(UUID leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEAVE_REQUEST_NOT_FOUND,
                        "Leave request not found with ID: " + leaveRequestId));
        return leaveRequestMapper.toResponse(leaveRequest);
    }

    @Override
    public List<LeaveRequestResponse> listLeaveRequests(UUID doctorId, LeaveRequestStatus status) {
        List<LeaveRequest> list;
        if (doctorId != null) {
            list = leaveRequestRepository.findByDoctorIdOrderByStartDateTimeDesc(doctorId);
            if (status != null) {
                list = list.stream().filter(lr -> lr.getStatus() == status).toList();
            }
        } else if (status != null) {
            list = leaveRequestRepository.findByStatusOrderByStartDateTimeDesc(status);
        } else {
            list = leaveRequestRepository.findAll();
        }
        return leaveRequestMapper.toResponseList(list);
    }

    @Override
    @Transactional
    public ApprovedLeaveResult approveLeaveRequest(UUID leaveRequestId, ReviewLeaveRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEAVE_REQUEST_NOT_FOUND,
                        "Leave request not found with ID: " + leaveRequestId));

        leaveRequestValidator.validateReview(request, leaveRequest);

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setReviewedBy(request.getReviewedBy().trim());
        leaveRequest.setReviewedAt(Instant.now());

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        LeaveRequestResponse leaveResponse = leaveRequestMapper.toResponse(updated);

        ApprovedLeaveResult result = processAffectedSchedules(leaveRequest, leaveResponse, true);

        eventPublisher.publish(EventEnvelope.of(
                "LeaveRequest.Approved",
                "scheduling-service",
                new LeaveRequestApprovedEventPayload(
                        updated.getLeaveRequestId(),
                        updated.getDoctorId(),
                        updated.getStartDateTime().toLocalDate(),
                        updated.getEndDateTime().toLocalDate()
                )
        ));

        return result;
    }

    @Override
    public ApprovedLeaveResult getAffectedSchedules(UUID leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEAVE_REQUEST_NOT_FOUND,
                        "Leave request not found with ID: " + leaveRequestId));

        LeaveRequestResponse leaveResponse = leaveRequestMapper.toResponse(leaveRequest);
        return processAffectedSchedules(leaveRequest, leaveResponse, false);
    }

    private ApprovedLeaveResult processAffectedSchedules(
            LeaveRequest leaveRequest,
            LeaveRequestResponse leaveResponse,
            boolean applyMutation
    ) {
        LocalDate startDate = leaveRequest.getStartDateTime().toLocalDate();
        LocalDate endDate = leaveRequest.getEndDateTime().toLocalDate();

        List<WorkSchedule> candidateSchedules = workScheduleRepository.findApprovedSchedulesInDateRange(
                leaveRequest.getDoctorId(),
                startDate,
                endDate
        );

        List<AffectedScheduleDto> affectedList = new ArrayList<>();
        int cancelledCount = 0;
        int schedulesWithAppointmentsCount = 0;
        int totalAppointmentsCount = 0;

        for (WorkSchedule ws : candidateSchedules) {
            LocalDateTime scheduleStart = LocalDateTime.of(ws.getDate(), ws.getStartTime());
            LocalDateTime scheduleEnd = LocalDateTime.of(ws.getDate(), ws.getEndTime());

            // Check overlap between schedule and leave window
            if (scheduleStart.isBefore(leaveRequest.getEndDateTime()) && scheduleEnd.isAfter(leaveRequest.getStartDateTime())) {
                List<Slot> slots = slotRepository.findByWorkSchedule_ScheduleId(ws.getScheduleId());
                List<UUID> bookedSlotIds = slots.stream()
                        .filter(s -> s.getStatus() == SlotStatus.BOOKED || s.getStatus() == SlotStatus.HELD)
                        .map(Slot::getSlotId)
                        .toList();

                if (bookedSlotIds.isEmpty()) {
                    // BR-LEAVE-004 / WF-03 Case A: Schedule has NO appointments -> CANCEL schedule and BLOCK slots
                    if (applyMutation) {
                        ws.setStatus(WorkScheduleStatus.CANCELLED);
                        workScheduleRepository.save(ws);

                        for (Slot slot : slots) {
                            if (slot.getStatus() == SlotStatus.AVAILABLE) {
                                slot.setStatus(SlotStatus.BLOCKED);
                                slotRepository.save(slot);
                            }
                        }
                    }
                    cancelledCount++;
                    affectedList.add(new AffectedScheduleDto(
                            ws.getScheduleId(),
                            ws.getDate(),
                            ws.getStartTime(),
                            ws.getEndTime(),
                            ws.getClinic().getClinicId(),
                            ws.getRoom().getRoomId(),
                            ws.getSpecialty().getSpecialtyId(),
                            "CANCELLED_NO_APPOINTMENTS",
                            List.of()
                    ));
                } else {
                    // BR-LEAVE-005 / WF-03 Case B: Schedule HAS appointments -> PRESERVE! Do NOT cancel or delete!
                    schedulesWithAppointmentsCount++;
                    totalAppointmentsCount += bookedSlotIds.size();
                    affectedList.add(new AffectedScheduleDto(
                            ws.getScheduleId(),
                            ws.getDate(),
                            ws.getStartTime(),
                            ws.getEndTime(),
                            ws.getClinic().getClinicId(),
                            ws.getRoom().getRoomId(),
                            ws.getSpecialty().getSpecialtyId(),
                            "PRESERVED_HAS_APPOINTMENTS",
                            bookedSlotIds
                    ));
                }
            }
        }

        return new ApprovedLeaveResult(
                leaveResponse,
                affectedList,
                cancelledCount,
                schedulesWithAppointmentsCount,
                totalAppointmentsCount
        );
    }

    @Override
    @Transactional
    public LeaveRequestResponse rejectLeaveRequest(UUID leaveRequestId, ReviewLeaveRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEAVE_REQUEST_NOT_FOUND,
                        "Leave request not found with ID: " + leaveRequestId));

        leaveRequestValidator.validateReview(request, leaveRequest);

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setReviewedBy(request.getReviewedBy().trim());
        leaveRequest.setReviewedAt(Instant.now());

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return leaveRequestMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public LeaveRequestResponse cancelLeaveRequest(UUID leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEAVE_REQUEST_NOT_FOUND,
                        "Leave request not found with ID: " + leaveRequestId));

        leaveRequestValidator.validateCancel(leaveRequest);

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return leaveRequestMapper.toResponse(updated);
    }
}
