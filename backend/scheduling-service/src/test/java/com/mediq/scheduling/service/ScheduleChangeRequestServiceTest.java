package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateScheduleChangeRequest;
import com.mediq.scheduling.dto.request.ReviewScheduleChangeRequest;
import com.mediq.scheduling.dto.response.ScheduleChangeRequestResponse;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.ClinicStatus;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.RoomStatus;
import com.mediq.scheduling.entity.ScheduleChangeRequest;
import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.SpecialtyStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleChangeRequestServiceTest {

    @Mock
    private ScheduleChangeRequestRepository scheduleChangeRequestRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private SlotService slotService;

    private ScheduleChangeRequestValidator validator;
    private ScheduleChangeRequestMapper mapper;
    private ScheduleChangeRequestServiceImpl service;

    private UUID scheduleId;
    private UUID doctorId;
    private UUID clinicId;
    private UUID specialtyId;
    private UUID roomId;
    private Clinic clinic;
    private Specialty specialty;
    private Room room;
    private WorkSchedule schedule;

    @BeforeEach
    void setUp() {
        validator = new ScheduleChangeRequestValidator();
        mapper = new ScheduleChangeRequestMapper();
        service = new ScheduleChangeRequestServiceImpl(
                scheduleChangeRequestRepository,
                workScheduleRepository,
                roomRepository,
                slotRepository,
                slotService,
                validator,
                mapper
        );

        scheduleId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
        roomId = UUID.randomUUID();

        clinic = Clinic.builder().clinicId(clinicId).name("Main Clinic").status(ClinicStatus.ACTIVE).build();
        specialty = Specialty.builder().specialtyId(specialtyId).name("Cardiology").status(SpecialtyStatus.ACTIVE).build();
        room = Room.builder().roomId(roomId).code("ROOM-101").clinic(clinic).specialty(specialty).status(RoomStatus.ACTIVE).build();

        schedule = WorkSchedule.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .clinic(clinic)
                .specialty(specialty)
                .room(room)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .status(WorkScheduleStatus.APPROVED)
                .build();
    }

    @Test
    @DisplayName("Should create schedule change request successfully")
    void shouldCreateScheduleChangeRequestSuccessfully() {
        CreateScheduleChangeRequest request = CreateScheduleChangeRequest.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .requestedRoomId(null)
                .reason("Traffic delay")
                .build();

        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleChangeRequestRepository.existsBySchedule_ScheduleIdAndStatus(scheduleId, ScheduleChangeRequestStatus.PENDING))
                .thenReturn(false);
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class)))
                .thenAnswer(invocation -> {
                    ScheduleChangeRequest scr = invocation.getArgument(0);
                    scr.setRequestId(UUID.randomUUID());
                    return scr;
                });

        ScheduleChangeRequestResponse response = service.createRequest(request);

        assertNotNull(response);
        assertEquals(scheduleId, response.scheduleId());
        assertEquals(doctorId, response.doctorId());
        assertEquals(LocalTime.of(9, 0), response.requestedStartTime());
        assertEquals(LocalTime.of(13, 0), response.requestedEndTime());
        assertEquals(ScheduleChangeRequestStatus.PENDING, response.status());
        verify(scheduleChangeRequestRepository).save(any(ScheduleChangeRequest.class));
    }

    @Test
    @DisplayName("Should reject create request when doctor ID does not match work schedule")
    void shouldRejectCreateWhenDoctorMismatch() {
        UUID otherDoctorId = UUID.randomUUID();
        CreateScheduleChangeRequest request = CreateScheduleChangeRequest.builder()
                .scheduleId(scheduleId)
                .doctorId(otherDoctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .build();

        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleChangeRequestRepository.existsBySchedule_ScheduleIdAndStatus(scheduleId, ScheduleChangeRequestStatus.PENDING))
                .thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createRequest(request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject create request when an active pending change request already exists")
    void shouldRejectCreateWhenActivePendingRequestExists() {
        CreateScheduleChangeRequest request = CreateScheduleChangeRequest.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .build();

        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleChangeRequestRepository.existsBySchedule_ScheduleIdAndStatus(scheduleId, ScheduleChangeRequestStatus.PENDING))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createRequest(request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject create request when requested room does not belong to schedule clinic")
    void shouldRejectCreateWhenRequestedRoomClinicMismatch() {
        UUID otherClinicId = UUID.randomUUID();
        Clinic otherClinic = Clinic.builder().clinicId(otherClinicId).name("Other Clinic").status(ClinicStatus.ACTIVE).build();
        UUID otherRoomId = UUID.randomUUID();
        Room otherRoom = Room.builder().roomId(otherRoomId).clinic(otherClinic).specialty(specialty).status(RoomStatus.ACTIVE).build();

        CreateScheduleChangeRequest request = CreateScheduleChangeRequest.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .requestedRoomId(otherRoomId)
                .build();

        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(roomRepository.findById(otherRoomId)).thenReturn(Optional.of(otherRoom));
        when(scheduleChangeRequestRepository.existsBySchedule_ScheduleIdAndStatus(scheduleId, ScheduleChangeRequestStatus.PENDING))
                .thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createRequest(request));
        assertEquals(ErrorCode.ROOM_CLINIC_MISMATCH, ex.getErrorCode());
        verify(scheduleChangeRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should approve schedule change request successfully when no conflicts and no booked appointments exist (WF-08)")
    void shouldApproveScheduleChangeRequestSuccessfully() {
        UUID requestId = UUID.randomUUID();
        LocalTime newStartTime = LocalTime.of(9, 0);
        LocalTime newEndTime = LocalTime.of(13, 0);

        ScheduleChangeRequest changeRequest = ScheduleChangeRequest.builder()
                .requestId(requestId)
                .schedule(schedule)
                .doctorId(doctorId)
                .requestedStartTime(newStartTime)
                .requestedEndTime(newEndTime)
                .status(ScheduleChangeRequestStatus.PENDING)
                .build();

        Slot availableSlot = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(schedule)
                .status(SlotStatus.AVAILABLE)
                .build();

        ReviewScheduleChangeRequest reviewRequest = new ReviewScheduleChangeRequest("admin-user", "Approved");

        when(scheduleChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(changeRequest));
        when(workScheduleRepository.hasDoctorOverlap(doctorId, schedule.getDate(), newStartTime, newEndTime, scheduleId))
                .thenReturn(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(workScheduleRepository.hasRoomOverlap(roomId, schedule.getDate(), newStartTime, newEndTime, scheduleId))
                .thenReturn(false);
        when(slotRepository.findByWorkSchedule_ScheduleId(scheduleId)).thenReturn(List.of(availableSlot));
        when(workScheduleRepository.save(any(WorkSchedule.class))).thenAnswer(i -> i.getArgument(0));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class))).thenAnswer(i -> i.getArgument(0));

        ScheduleChangeRequestResponse response = service.approveRequest(requestId, reviewRequest);

        assertNotNull(response);
        assertEquals(ScheduleChangeRequestStatus.APPROVED, response.status());
        assertEquals("admin-user", response.reviewedBy());
        assertNotNull(response.reviewedAt());

        // Verify schedule updated
        assertEquals(newStartTime, schedule.getStartTime());
        assertEquals(newEndTime, schedule.getEndTime());
        verify(workScheduleRepository).save(schedule);

        // Verify old available slots deleted and regenerated
        verify(slotRepository).deleteAll(List.of(availableSlot));
        verify(slotService).generateSlotsForSchedule(schedule);
        verify(scheduleChangeRequestRepository).save(changeRequest);
    }

    @Test
    @DisplayName("Should reject approve when schedule has booked appointments (WF-08)")
    void shouldRejectApproveWhenScheduleHasBookedAppointments() {
        UUID requestId = UUID.randomUUID();
        LocalTime newStartTime = LocalTime.of(9, 0);
        LocalTime newEndTime = LocalTime.of(13, 0);

        ScheduleChangeRequest changeRequest = ScheduleChangeRequest.builder()
                .requestId(requestId)
                .schedule(schedule)
                .doctorId(doctorId)
                .requestedStartTime(newStartTime)
                .requestedEndTime(newEndTime)
                .status(ScheduleChangeRequestStatus.PENDING)
                .build();

        Slot bookedSlot = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(schedule)
                .status(SlotStatus.BOOKED)
                .build();

        ReviewScheduleChangeRequest reviewRequest = new ReviewScheduleChangeRequest("admin-user", "Approved");

        when(scheduleChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(changeRequest));
        when(workScheduleRepository.hasDoctorOverlap(doctorId, schedule.getDate(), newStartTime, newEndTime, scheduleId))
                .thenReturn(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(workScheduleRepository.hasRoomOverlap(roomId, schedule.getDate(), newStartTime, newEndTime, scheduleId))
                .thenReturn(false);
        when(slotRepository.findByWorkSchedule_ScheduleId(scheduleId)).thenReturn(List.of(bookedSlot));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.approveRequest(requestId, reviewRequest));
        assertEquals(ErrorCode.SCHEDULE_HAS_BOOKED_APPOINTMENTS, ex.getErrorCode());

        // Schedule and slots must not be touched
        verify(workScheduleRepository, never()).save(any());
        verify(slotRepository, never()).deleteAll(any());
        assertEquals(ScheduleChangeRequestStatus.PENDING, changeRequest.getStatus());
    }

    @Test
    @DisplayName("Should reject approve when new times cause doctor schedule overlap")
    void shouldRejectApproveWhenDoctorOverlap() {
        UUID requestId = UUID.randomUUID();
        LocalTime newStartTime = LocalTime.of(9, 0);
        LocalTime newEndTime = LocalTime.of(13, 0);

        ScheduleChangeRequest changeRequest = ScheduleChangeRequest.builder()
                .requestId(requestId)
                .schedule(schedule)
                .doctorId(doctorId)
                .requestedStartTime(newStartTime)
                .requestedEndTime(newEndTime)
                .status(ScheduleChangeRequestStatus.PENDING)
                .build();

        ReviewScheduleChangeRequest reviewRequest = new ReviewScheduleChangeRequest("admin-user", "Approved");

        when(scheduleChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(changeRequest));
        when(workScheduleRepository.hasDoctorOverlap(doctorId, schedule.getDate(), newStartTime, newEndTime, scheduleId))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.approveRequest(requestId, reviewRequest));
        assertEquals(ErrorCode.DOCTOR_SCHEDULE_OVERLAP, ex.getErrorCode());
        verify(workScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject approve when doctor attempts to approve their own request")
    void shouldRejectApproveWhenDoctorApprovesOwnRequest() {
        UUID requestId = UUID.randomUUID();
        ScheduleChangeRequest changeRequest = ScheduleChangeRequest.builder()
                .requestId(requestId)
                .schedule(schedule)
                .doctorId(doctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .status(ScheduleChangeRequestStatus.PENDING)
                .build();

        ReviewScheduleChangeRequest reviewRequest = new ReviewScheduleChangeRequest(doctorId.toString(), "Self approve");

        when(scheduleChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(changeRequest));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.approveRequest(requestId, reviewRequest));
        assertEquals(ErrorCode.DOCTOR_CANNOT_APPROVE_OWN_SCHEDULE_CHANGE, ex.getErrorCode());
        verify(workScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject schedule change request successfully")
    void shouldRejectScheduleChangeRequestSuccessfully() {
        UUID requestId = UUID.randomUUID();
        ScheduleChangeRequest changeRequest = ScheduleChangeRequest.builder()
                .requestId(requestId)
                .schedule(schedule)
                .doctorId(doctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .status(ScheduleChangeRequestStatus.PENDING)
                .build();

        ReviewScheduleChangeRequest reviewRequest = new ReviewScheduleChangeRequest("admin-user", "Cannot accommodate");

        when(scheduleChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(changeRequest));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class))).thenAnswer(i -> i.getArgument(0));

        ScheduleChangeRequestResponse response = service.rejectRequest(requestId, reviewRequest);

        assertNotNull(response);
        assertEquals(ScheduleChangeRequestStatus.REJECTED, response.status());
        assertEquals("admin-user", response.reviewedBy());
        assertNotNull(response.reviewedAt());
        verify(workScheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel schedule change request successfully")
    void shouldCancelScheduleChangeRequestSuccessfully() {
        UUID requestId = UUID.randomUUID();
        ScheduleChangeRequest changeRequest = ScheduleChangeRequest.builder()
                .requestId(requestId)
                .schedule(schedule)
                .doctorId(doctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .status(ScheduleChangeRequestStatus.PENDING)
                .build();

        when(scheduleChangeRequestRepository.findById(requestId)).thenReturn(Optional.of(changeRequest));
        when(scheduleChangeRequestRepository.save(any(ScheduleChangeRequest.class))).thenAnswer(i -> i.getArgument(0));

        ScheduleChangeRequestResponse response = service.cancelRequest(requestId);

        assertNotNull(response);
        assertEquals(ScheduleChangeRequestStatus.CANCELLED, response.status());
        verify(workScheduleRepository, never()).save(any());
    }
}
