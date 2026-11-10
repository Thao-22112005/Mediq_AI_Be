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
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.WorkScheduleMapper;
import com.mediq.scheduling.repository.ClinicRepository;
import com.mediq.scheduling.repository.RoomRepository;
import com.mediq.scheduling.repository.SpecialtyRepository;
import com.mediq.scheduling.repository.WorkScheduleRepository;
import com.mediq.scheduling.validation.WorkScheduleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkScheduleServiceTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DoctorClient doctorClient;

    @Mock
    private WorkScheduleMapper workScheduleMapper;

    @Mock
    private SlotService slotService;

    @Mock
    private com.mediq.scheduling.event.EventPublisher eventPublisher;

    private WorkScheduleValidator workScheduleValidator;
    private WorkScheduleServiceImpl workScheduleService;

    private UUID doctorId;
    private UUID clinicId;
    private UUID specialtyId;
    private UUID roomId;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private Clinic clinic;
    private Specialty specialty;
    private Room room;

    @BeforeEach
    void setUp() {
        workScheduleValidator = new WorkScheduleValidator();
        workScheduleService = new WorkScheduleServiceImpl(
                workScheduleRepository,
                clinicRepository,
                specialtyRepository,
                roomRepository,
                doctorClient,
                workScheduleMapper,
                workScheduleValidator,
                slotService,
                eventPublisher
        );

        doctorId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        scheduleDate = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(8, 0);
        endTime = LocalTime.of(12, 0);

        clinic = Clinic.builder()
                .clinicId(clinicId)
                .code("CLN-01")
                .status(ClinicStatus.ACTIVE)
                .build();

        specialty = Specialty.builder()
                .specialtyId(specialtyId)
                .name("Cardiology")
                .status(SpecialtyStatus.ACTIVE)
                .build();

        room = Room.builder()
                .roomId(roomId)
                .clinic(clinic)
                .specialty(specialty)
                .code("R-101")
                .status(RoomStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should create work schedule successfully when all business rules are satisfied")
    void shouldCreateWorkScheduleSuccessfully() {
        CreateWorkScheduleRequest request = CreateWorkScheduleRequest.builder()
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(doctorClient.hasSpecialty(doctorId, specialtyId)).thenReturn(true);
        when(workScheduleRepository.hasDoctorOverlap(doctorId, scheduleDate, startTime, endTime, null)).thenReturn(false);
        when(workScheduleRepository.hasRoomOverlap(roomId, scheduleDate, startTime, endTime, null)).thenReturn(false);

        UUID scheduleId = UUID.randomUUID();
        WorkSchedule savedSchedule = WorkSchedule.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .clinic(clinic)
                .specialty(specialty)
                .room(room)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(WorkScheduleStatus.PENDING)
                .build();

        when(workScheduleRepository.save(any(WorkSchedule.class))).thenReturn(savedSchedule);

        WorkScheduleResponse expectedResponse = new WorkScheduleResponse(
                scheduleId, doctorId, clinicId, specialtyId, roomId,
                scheduleDate, startTime, endTime, WorkScheduleStatus.PENDING,
                Instant.now(), Instant.now()
        );
        when(workScheduleMapper.toWorkScheduleResponse(savedSchedule)).thenReturn(expectedResponse);

        WorkScheduleResponse response = workScheduleService.createWorkSchedule(request);

        assertNotNull(response);
        assertEquals(WorkScheduleStatus.PENDING, response.status());
        assertEquals(doctorId, response.doctorId());
        verify(workScheduleRepository).save(any(WorkSchedule.class));
    }

    @Test
    @DisplayName("Should reject when Room does not belong to specified Clinic (BR-SCHEDULE-004)")
    void shouldRejectWhenRoomClinicMismatch() {
        UUID otherClinicId = UUID.randomUUID();
        Clinic otherClinic = Clinic.builder()
                .clinicId(otherClinicId)
                .code("CLN-OTHER")
                .status(ClinicStatus.ACTIVE)
                .build();

        Room roomInOtherClinic = Room.builder()
                .roomId(roomId)
                .clinic(otherClinic)
                .specialty(specialty)
                .status(RoomStatus.ACTIVE)
                .build();

        CreateWorkScheduleRequest request = CreateWorkScheduleRequest.builder()
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomInOtherClinic));

        BusinessException ex = assertThrows(BusinessException.class, () -> workScheduleService.createWorkSchedule(request));
        assertEquals(ErrorCode.ROOM_CLINIC_MISMATCH, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject when Room specialty does not match schedule specialty (BR-SCHEDULE-005)")
    void shouldRejectWhenRoomSpecialtyMismatch() {
        Specialty otherSpecialty = Specialty.builder()
                .specialtyId(UUID.randomUUID())
                .name("Dermatology")
                .status(SpecialtyStatus.ACTIVE)
                .build();

        Room roomWithOtherSpecialty = Room.builder()
                .roomId(roomId)
                .clinic(clinic)
                .specialty(otherSpecialty)
                .status(RoomStatus.ACTIVE)
                .build();

        CreateWorkScheduleRequest request = CreateWorkScheduleRequest.builder()
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomWithOtherSpecialty));

        BusinessException ex = assertThrows(BusinessException.class, () -> workScheduleService.createWorkSchedule(request));
        assertEquals(ErrorCode.ROOM_SPECIALTY_MISMATCH, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject when Doctor does not have required specialty (BR-SCHEDULE-006)")
    void shouldRejectWhenDoctorSpecialtyMismatch() {
        CreateWorkScheduleRequest request = CreateWorkScheduleRequest.builder()
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(doctorClient.hasSpecialty(doctorId, specialtyId)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> workScheduleService.createWorkSchedule(request));
        assertEquals(ErrorCode.DOCTOR_SPECIALTY_MISMATCH, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject when Doctor has overlapping schedule (BR-SCHEDULE-007)")
    void shouldRejectWhenDoctorScheduleOverlap() {
        CreateWorkScheduleRequest request = CreateWorkScheduleRequest.builder()
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(doctorClient.hasSpecialty(doctorId, specialtyId)).thenReturn(true);
        when(workScheduleRepository.hasDoctorOverlap(doctorId, scheduleDate, startTime, endTime, null)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> workScheduleService.createWorkSchedule(request));
        assertEquals(ErrorCode.DOCTOR_SCHEDULE_OVERLAP, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject when Room has overlapping schedule (BR-SCHEDULE-008)")
    void shouldRejectWhenRoomScheduleOverlap() {
        CreateWorkScheduleRequest request = CreateWorkScheduleRequest.builder()
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(doctorClient.hasSpecialty(doctorId, specialtyId)).thenReturn(true);
        when(workScheduleRepository.hasDoctorOverlap(doctorId, scheduleDate, startTime, endTime, null)).thenReturn(false);
        when(workScheduleRepository.hasRoomOverlap(roomId, scheduleDate, startTime, endTime, null)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> workScheduleService.createWorkSchedule(request));
        assertEquals(ErrorCode.ROOM_SCHEDULE_OVERLAP, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should approve work schedule successfully")
    void shouldApproveWorkScheduleSuccessfully() {
        UUID scheduleId = UUID.randomUUID();
        WorkSchedule schedule = WorkSchedule.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .clinic(clinic)
                .specialty(specialty)
                .room(room)
                .date(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(WorkScheduleStatus.PENDING)
                .build();

        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(workScheduleRepository.hasDoctorOverlap(doctorId, scheduleDate, startTime, endTime, scheduleId)).thenReturn(false);
        when(workScheduleRepository.hasRoomOverlap(roomId, scheduleDate, startTime, endTime, scheduleId)).thenReturn(false);
        when(workScheduleRepository.save(any(WorkSchedule.class))).thenReturn(schedule);

        WorkScheduleResponse approvedResponse = new WorkScheduleResponse(
                scheduleId, doctorId, clinicId, specialtyId, roomId,
                scheduleDate, startTime, endTime, WorkScheduleStatus.APPROVED,
                Instant.now(), Instant.now()
        );
        when(workScheduleMapper.toWorkScheduleResponse(schedule)).thenReturn(approvedResponse);

        WorkScheduleResponse response = workScheduleService.approveWorkSchedule(scheduleId);

        assertNotNull(response);
        assertEquals(WorkScheduleStatus.APPROVED, response.status());
        verify(eventPublisher).publish(argThat(event -> "Schedule.Approved".equals(event.eventType())));
    }

    @Test
    @DisplayName("Should cancel work schedule successfully preserving history (BR-SCHEDULE-009)")
    void shouldCancelWorkScheduleSuccessfully() {
        UUID scheduleId = UUID.randomUUID();
        WorkSchedule schedule = WorkSchedule.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .clinic(clinic)
                .status(WorkScheduleStatus.APPROVED)
                .build();

        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(workScheduleRepository.save(any(WorkSchedule.class))).thenReturn(schedule);

        WorkScheduleResponse cancelledResponse = new WorkScheduleResponse(
                scheduleId, doctorId, clinicId, specialtyId, roomId,
                scheduleDate, startTime, endTime, WorkScheduleStatus.CANCELLED,
                Instant.now(), Instant.now()
        );
        when(workScheduleMapper.toWorkScheduleResponse(schedule)).thenReturn(cancelledResponse);

        WorkScheduleResponse response = workScheduleService.cancelWorkSchedule(scheduleId);

        assertNotNull(response);
        assertEquals(WorkScheduleStatus.CANCELLED, response.status());
        verify(eventPublisher).publish(argThat(event -> "Schedule.Cancelled".equals(event.eventType())));
    }
}
