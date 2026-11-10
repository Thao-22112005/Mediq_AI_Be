
package com.mediq.scheduling.service;

import com.mediq.scheduling.client.DoctorClient;
import com.mediq.scheduling.dto.request.CreateWorkScheduleRequest;
import com.mediq.scheduling.dto.response.WorkScheduleResponse;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.ClinicStatus;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.RoomStatus;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.SpecialtyStatus;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.event.EventEnvelope;
import com.mediq.scheduling.event.EventPublisher;
import com.mediq.scheduling.event.payload.ScheduleApprovedEventPayload;
import com.mediq.scheduling.event.payload.ScheduleCancelledEventPayload;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.WorkScheduleMapper;
import com.mediq.scheduling.repository.ClinicRepository;
import com.mediq.scheduling.repository.RoomRepository;
import com.mediq.scheduling.repository.SpecialtyRepository;
import com.mediq.scheduling.repository.WorkScheduleRepository;
import com.mediq.scheduling.validation.WorkScheduleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final ClinicRepository clinicRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RoomRepository roomRepository;
    private final DoctorClient doctorClient;
    private final WorkScheduleMapper workScheduleMapper;
    private final WorkScheduleValidator workScheduleValidator;
    private final SlotService slotService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public WorkScheduleResponse createWorkSchedule(
            CreateWorkScheduleRequest request
    ) {
        workScheduleValidator.validateCreate(request);

        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CLINIC_NOT_FOUND,
                        "Clinic not found with ID: " + request.getClinicId()
                ));

        if (clinic.getStatus() != ClinicStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CLINIC_INACTIVE,
                    "Clinic is currently inactive"
            );
        }

        Specialty specialty = specialtyRepository.findById(
                request.getSpecialtyId()
        ).orElseThrow(() -> new BusinessException(
                ErrorCode.SPECIALTY_NOT_FOUND,
                "Specialty not found with ID: "
                        + request.getSpecialtyId()
        ));

        if (specialty.getStatus() != SpecialtyStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.SPECIALTY_INACTIVE,
                    "Specialty is currently inactive"
            );
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ROOM_NOT_FOUND,
                        "Room not found with ID: " + request.getRoomId()
                ));

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ROOM_NOT_FOUND,
                    "Room is not active or available"
            );
        }

        // BR-SCHEDULE-004: Room must belong to the specified clinic
        if (!room.getClinic().getClinicId()
                .equals(clinic.getClinicId())) {
            throw new BusinessException(
                    ErrorCode.ROOM_CLINIC_MISMATCH,
                    "Room " + room.getCode()
                            + " does not belong to clinic "
                            + clinic.getCode()
            );
        }

        // BR-SCHEDULE-005: Room specialty must match schedule specialty
        if (!room.getSpecialty().getSpecialtyId()
                .equals(specialty.getSpecialtyId())) {
            throw new BusinessException(
                    ErrorCode.ROOM_SPECIALTY_MISMATCH,
                    "Room specialty ("
                            + room.getSpecialty().getName()
                            + ") does not match schedule specialty ("
                            + specialty.getName() + ")"
            );
        }

        // BR-SCHEDULE-006: Doctor must have a compatible specialty
        boolean hasSpecialty = doctorClient.hasSpecialty(
                request.getDoctorId(),
                specialty.getSpecialtyId()
        );

        if (!hasSpecialty) {
            throw new BusinessException(
                    ErrorCode.DOCTOR_SPECIALTY_MISMATCH,
                    "Doctor does not have the required specialty"
                            + " for this schedule"
            );
        }

        // BR-SCHEDULE-007: Doctor cannot have overlapping schedules
        boolean doctorOverlap =
                workScheduleRepository.hasDoctorOverlap(
                        request.getDoctorId(),
                        request.getDate(),
                        request.getStartTime(),
                        request.getEndTime(),
                        null
                );

        if (doctorOverlap) {
            throw new BusinessException(
                    ErrorCode.DOCTOR_SCHEDULE_OVERLAP,
                    "Doctor already has an overlapping work schedule on "
                            + request.getDate()
            );
        }

        // BR-SCHEDULE-008: Room cannot have overlapping schedules
        boolean roomOverlap =
                workScheduleRepository.hasRoomOverlap(
                        room.getRoomId(),
                        request.getDate(),
                        request.getStartTime(),
                        request.getEndTime(),
                        null
                );

        if (roomOverlap) {
            throw new BusinessException(
                    ErrorCode.ROOM_SCHEDULE_OVERLAP,
                    "Room " + room.getCode()
                            + " already has an overlapping work schedule on "
                            + request.getDate()
            );
        }

        WorkSchedule schedule = WorkSchedule.builder()
                .doctorId(request.getDoctorId())
                .clinic(clinic)
                .specialty(specialty)
                .room(room)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(WorkScheduleStatus.APPROVED)
                .build();

        WorkSchedule savedSchedule =
                workScheduleRepository.save(schedule);

        // Generate slots immediately for the approved schedule
        slotService.generateSlotsForSchedule(savedSchedule);

        // Publish event for related services
        eventPublisher.publish(EventEnvelope.of(
                "Schedule.Approved",
                "scheduling-service",
                new ScheduleApprovedEventPayload(
                        savedSchedule.getScheduleId(),
                        savedSchedule.getDoctorId(),
                        savedSchedule.getClinic().getClinicId(),
                        savedSchedule.getRoom().getRoomId(),
                        savedSchedule.getSpecialty().getSpecialtyId(),
                        savedSchedule.getDate(),
                        savedSchedule.getStartTime(),
                        savedSchedule.getEndTime()
                )
        ));

        return toResponse(savedSchedule);
    }

    @Override
    public WorkScheduleResponse getWorkScheduleById(UUID scheduleId) {
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SCHEDULE_NOT_FOUND,
                        "Work schedule not found with ID: " + scheduleId
                ));

        return toResponse(schedule);
    }

    @Override
    public List<WorkScheduleResponse> getWorkSchedules(
            UUID doctorId,
            UUID clinicId,
            UUID roomId,
            LocalDate date,
            WorkScheduleStatus status
    ) {
        List<WorkSchedule> filtered =
                workScheduleRepository.findAll()
                        .stream()
                        .filter(s -> doctorId == null
                                || doctorId.equals(s.getDoctorId()))
                        .filter(s -> clinicId == null
                                || clinicId.equals(
                                s.getClinic().getClinicId()))
                        .filter(s -> roomId == null
                                || roomId.equals(
                                s.getRoom().getRoomId()))
                        .filter(s -> date == null
                                || date.equals(s.getDate()))
                        .filter(s -> status == null
                                || status == s.getStatus())
                        .toList();

        return filtered.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public WorkScheduleResponse approveWorkSchedule(UUID scheduleId) {
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SCHEDULE_NOT_FOUND,
                        "Work schedule not found with ID: " + scheduleId
                ));

        if (schedule.getStatus() == WorkScheduleStatus.APPROVED) {
            throw new BusinessException(
                    ErrorCode.SCHEDULE_ALREADY_APPROVED,
                    "Work schedule is already approved"
            );
        }

        if (schedule.getStatus() == WorkScheduleStatus.CANCELLED) {
            throw new BusinessException(
                    ErrorCode.SCHEDULE_ALREADY_CANCELLED,
                    "Cannot approve a cancelled work schedule"
            );
        }

        // Re-check doctor overlap before approval
        boolean doctorOverlap =
                workScheduleRepository.hasDoctorOverlap(
                        schedule.getDoctorId(),
                        schedule.getDate(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getScheduleId()
                );

        if (doctorOverlap) {
            throw new BusinessException(
                    ErrorCode.DOCTOR_SCHEDULE_OVERLAP,
                    "Doctor already has an approved overlapping work schedule on "
                            + schedule.getDate()
            );
        }

        // Re-check room overlap before approval
        boolean roomOverlap =
                workScheduleRepository.hasRoomOverlap(
                        schedule.getRoom().getRoomId(),
                        schedule.getDate(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getScheduleId()
                );

        if (roomOverlap) {
            throw new BusinessException(
                    ErrorCode.ROOM_SCHEDULE_OVERLAP,
                    "Room already has an approved overlapping work schedule on "
                            + schedule.getDate()
            );
        }

        schedule.setStatus(WorkScheduleStatus.APPROVED);

        WorkSchedule updatedSchedule =
                workScheduleRepository.save(schedule);

        // Generate slots for approved schedule
        slotService.generateSlotsForSchedule(updatedSchedule);

        eventPublisher.publish(EventEnvelope.of(
                "Schedule.Approved",
                "scheduling-service",
                new ScheduleApprovedEventPayload(
                        updatedSchedule.getScheduleId(),
                        updatedSchedule.getDoctorId(),
                        updatedSchedule.getClinic().getClinicId(),
                        updatedSchedule.getRoom().getRoomId(),
                        updatedSchedule.getSpecialty().getSpecialtyId(),
                        updatedSchedule.getDate(),
                        updatedSchedule.getStartTime(),
                        updatedSchedule.getEndTime()
                )
        ));

        return toResponse(updatedSchedule);
    }

    @Override
    @Transactional
    public WorkScheduleResponse cancelWorkSchedule(UUID scheduleId) {
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SCHEDULE_NOT_FOUND,
                        "Work schedule not found with ID: " + scheduleId
                ));

        if (schedule.getStatus() == WorkScheduleStatus.CANCELLED) {
            throw new BusinessException(
                    ErrorCode.SCHEDULE_ALREADY_CANCELLED,
                    "Work schedule is already cancelled"
            );
        }

        // Preserve schedule history instead of physically deleting it
        schedule.setStatus(WorkScheduleStatus.CANCELLED);

        WorkSchedule updatedSchedule =
                workScheduleRepository.save(schedule);

        eventPublisher.publish(EventEnvelope.of(
                "Schedule.Cancelled",
                "scheduling-service",
                new ScheduleCancelledEventPayload(
                        updatedSchedule.getScheduleId(),
                        updatedSchedule.getDoctorId(),
                        updatedSchedule.getClinic().getClinicId(),
                        updatedSchedule.getDate()
                )
        ));

        return toResponse(updatedSchedule);
    }

    /**
     * Centralized mapping for all WorkSchedule responses.
     * Names of clinic, room and specialty are mapped by WorkScheduleMapper.
     */
    private WorkScheduleResponse toResponse(WorkSchedule schedule) {
        return workScheduleMapper.toWorkScheduleResponse(schedule);
    }
}
