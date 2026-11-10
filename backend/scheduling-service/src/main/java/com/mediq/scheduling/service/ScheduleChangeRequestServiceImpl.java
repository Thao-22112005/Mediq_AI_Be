package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateScheduleChangeRequest;
import com.mediq.scheduling.dto.request.ReviewScheduleChangeRequest;
import com.mediq.scheduling.dto.response.ScheduleChangeRequestResponse;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.ScheduleChangeRequest;
import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.ScheduleChangeRequestMapper;
import com.mediq.scheduling.repository.RoomRepository;
import com.mediq.scheduling.repository.ScheduleChangeRequestRepository;
import com.mediq.scheduling.repository.SlotRepository;
import com.mediq.scheduling.repository.WorkScheduleRepository;
import com.mediq.scheduling.validation.ScheduleChangeRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleChangeRequestServiceImpl implements ScheduleChangeRequestService {

    private final ScheduleChangeRequestRepository scheduleChangeRequestRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final RoomRepository roomRepository;
    private final SlotRepository slotRepository;
    private final SlotService slotService;
    private final ScheduleChangeRequestValidator validator;
    private final ScheduleChangeRequestMapper mapper;

    @Override
    @Transactional
    public ScheduleChangeRequestResponse createRequest(CreateScheduleChangeRequest request) {
        WorkSchedule schedule = null;
        if (request != null && request.getScheduleId() != null) {
            schedule = workScheduleRepository.findById(request.getScheduleId()).orElse(null);
        }

        Room requestedRoom = null;
        if (request != null && request.getRequestedRoomId() != null) {
            requestedRoom = roomRepository.findById(request.getRequestedRoomId()).orElse(null);
        }

        boolean hasActivePending = false;
        if (request != null && request.getScheduleId() != null) {
            hasActivePending = scheduleChangeRequestRepository
                    .existsBySchedule_ScheduleIdAndStatus(request.getScheduleId(), ScheduleChangeRequestStatus.PENDING);
        }

        validator.validateCreate(request, schedule, requestedRoom, hasActivePending);

        ScheduleChangeRequest changeRequest = ScheduleChangeRequest.builder()
                .schedule(schedule)
                .doctorId(request.getDoctorId())
                .requestedStartTime(request.getRequestedStartTime())
                .requestedEndTime(request.getRequestedEndTime())
                .requestedRoomId(request.getRequestedRoomId())
                .reason(request.getReason())
                .status(ScheduleChangeRequestStatus.PENDING)
                .build();

        ScheduleChangeRequest saved = scheduleChangeRequestRepository.save(changeRequest);
        return mapper.toResponse(saved);
    }

    @Override
    public ScheduleChangeRequestResponse getRequestById(UUID requestId) {
        ScheduleChangeRequest changeRequest = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_CHANGE_REQUEST_NOT_FOUND,
                        "Schedule change request not found with ID: " + requestId));
        return mapper.toResponse(changeRequest);
    }

    @Override
    public List<ScheduleChangeRequestResponse> listRequests(UUID doctorId, ScheduleChangeRequestStatus status) {
        List<ScheduleChangeRequest> list;
        if (doctorId != null && status != null) {
            list = scheduleChangeRequestRepository.findByDoctorIdAndStatus(doctorId, status);
        } else if (doctorId != null) {
            list = scheduleChangeRequestRepository.findByDoctorId(doctorId);
        } else if (status != null) {
            list = scheduleChangeRequestRepository.findByStatus(status);
        } else {
            list = scheduleChangeRequestRepository.findAll();
        }
        return mapper.toResponseList(list);
    }

    @Override
    @Transactional
    public ScheduleChangeRequestResponse approveRequest(UUID requestId, ReviewScheduleChangeRequest request) {
        ScheduleChangeRequest changeRequest = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_CHANGE_REQUEST_NOT_FOUND,
                        "Schedule change request not found with ID: " + requestId));

        validator.validateReview(changeRequest, request);

        WorkSchedule schedule = changeRequest.getSchedule();

        // 1. Check Doctor overlap on new requested times
        boolean doctorOverlap = workScheduleRepository.hasDoctorOverlap(
                schedule.getDoctorId(),
                schedule.getDate(),
                changeRequest.getRequestedStartTime(),
                changeRequest.getRequestedEndTime(),
                schedule.getScheduleId()
        );
        if (doctorOverlap) {
            throw new BusinessException(ErrorCode.DOCTOR_SCHEDULE_OVERLAP,
                    "Doctor already has an overlapping work schedule on " + schedule.getDate());
        }

        // 2. Check Room overlap on new requested times and room
        UUID effectiveRoomId = changeRequest.getRequestedRoomId() != null
                ? changeRequest.getRequestedRoomId()
                : schedule.getRoom().getRoomId();

        Room effectiveRoom = roomRepository.findById(effectiveRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND,
                        "Room not found with ID: " + effectiveRoomId));

        boolean roomOverlap = workScheduleRepository.hasRoomOverlap(
                effectiveRoomId,
                schedule.getDate(),
                changeRequest.getRequestedStartTime(),
                changeRequest.getRequestedEndTime(),
                schedule.getScheduleId()
        );
        if (roomOverlap) {
            throw new BusinessException(ErrorCode.ROOM_SCHEDULE_OVERLAP,
                    "Room " + effectiveRoom.getCode() + " already has an overlapping work schedule on " + schedule.getDate());
        }

        // 3. Check existing slots for booked appointments (WF-08)
        List<Slot> existingSlots = slotRepository.findByWorkSchedule_ScheduleId(schedule.getScheduleId());
        boolean hasBookedSlots = existingSlots.stream()
                .anyMatch(s -> s.getStatus() == SlotStatus.BOOKED || s.getStatus() == SlotStatus.HELD);

        if (hasBookedSlots) {
            throw new BusinessException(ErrorCode.SCHEDULE_HAS_BOOKED_APPOINTMENTS,
                    "Cannot directly approve schedule change: work schedule has booked or held appointments that must be handled first.");
        }

        // 4. Update WorkSchedule parameters
        if (!existingSlots.isEmpty()) {
            slotRepository.deleteAll(existingSlots);
        }

        schedule.setStartTime(changeRequest.getRequestedStartTime());
        schedule.setEndTime(changeRequest.getRequestedEndTime());
        schedule.setRoom(effectiveRoom);
        workScheduleRepository.save(schedule);

        // 5. If schedule is APPROVED, regenerate slots for the new time window
        if (schedule.getStatus() == WorkScheduleStatus.APPROVED) {
            slotService.generateSlotsForSchedule(schedule);
        }

        // 6. Update change request status
        changeRequest.setStatus(ScheduleChangeRequestStatus.APPROVED);
        changeRequest.setReviewedBy(request.getReviewedBy().trim());
        changeRequest.setReviewedAt(Instant.now());
        ScheduleChangeRequest saved = scheduleChangeRequestRepository.save(changeRequest);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleChangeRequestResponse rejectRequest(UUID requestId, ReviewScheduleChangeRequest request) {
        ScheduleChangeRequest changeRequest = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_CHANGE_REQUEST_NOT_FOUND,
                        "Schedule change request not found with ID: " + requestId));

        validator.validateReview(changeRequest, request);

        changeRequest.setStatus(ScheduleChangeRequestStatus.REJECTED);
        changeRequest.setReviewedBy(request.getReviewedBy().trim());
        changeRequest.setReviewedAt(Instant.now());
        ScheduleChangeRequest saved = scheduleChangeRequestRepository.save(changeRequest);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ScheduleChangeRequestResponse cancelRequest(UUID requestId) {
        ScheduleChangeRequest changeRequest = scheduleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_CHANGE_REQUEST_NOT_FOUND,
                        "Schedule change request not found with ID: " + requestId));

        validator.validateCancel(changeRequest);

        changeRequest.setStatus(ScheduleChangeRequestStatus.CANCELLED);
        ScheduleChangeRequest saved = scheduleChangeRequestRepository.save(changeRequest);

        return mapper.toResponse(saved);
    }
}
