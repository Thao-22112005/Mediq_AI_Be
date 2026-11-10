package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateLeaveRequest;
import com.mediq.scheduling.dto.request.ReviewLeaveRequest;
import com.mediq.scheduling.dto.response.ApprovedLeaveResult;
import com.mediq.scheduling.dto.response.LeaveRequestResponse;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.LeaveRequest;
import com.mediq.scheduling.entity.LeaveRequestStatus;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.LeaveRequestMapper;
import com.mediq.scheduling.repository.LeaveRequestRepository;
import com.mediq.scheduling.repository.SlotRepository;
import com.mediq.scheduling.repository.WorkScheduleRepository;
import com.mediq.scheduling.validation.LeaveRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private com.mediq.scheduling.event.EventPublisher eventPublisher;

    private LeaveRequestValidator leaveRequestValidator;
    private LeaveRequestMapper leaveRequestMapper;
    private LeaveRequestServiceImpl leaveRequestService;

    private UUID doctorId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Clinic testClinic;
    private Room testRoom;
    private Specialty testSpecialty;

    @BeforeEach
    void setUp() {
        leaveRequestValidator = new LeaveRequestValidator();
        leaveRequestMapper = new LeaveRequestMapper();
        leaveRequestService = new LeaveRequestServiceImpl(
                leaveRequestRepository,
                leaveRequestValidator,
                leaveRequestMapper,
                workScheduleRepository,
                slotRepository,
                eventPublisher
        );

        doctorId = UUID.randomUUID();
        startDateTime = LocalDateTime.now().plusDays(2).withHour(8).withMinute(0);
        endDateTime = LocalDateTime.now().plusDays(5).withHour(17).withMinute(0);

        testClinic = Clinic.builder().clinicId(UUID.randomUUID()).name("Main Clinic").build();
        testRoom = Room.builder().roomId(UUID.randomUUID()).code("101").name("Room 101").build();
        testSpecialty = Specialty.builder().specialtyId(UUID.randomUUID()).name("Cardiology").build();
    }

    @Test
    @DisplayName("Should create leave request successfully with PENDING status")
    void shouldCreateLeaveRequestSuccessfully() {
        CreateLeaveRequest request = new CreateLeaveRequest(doctorId, startDateTime, endDateTime, "Annual vacation");

        when(leaveRequestRepository.hasOverlappingLeave(doctorId, startDateTime, endDateTime)).thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest lr = invocation.getArgument(0);
            lr.setLeaveRequestId(UUID.randomUUID());
            return lr;
        });

        LeaveRequestResponse response = leaveRequestService.createLeaveRequest(request);

        assertNotNull(response);
        assertEquals(doctorId, response.doctorId());
        assertEquals(LeaveRequestStatus.PENDING, response.status());
        assertEquals("Annual vacation", response.reason());
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("Should reject leave request in the past (PAST_LEAVE_NOT_ALLOWED)")
    void shouldRejectWhenStartDateTimeInPast() {
        LocalDateTime pastStart = LocalDateTime.now().minusDays(2);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(1);
        CreateLeaveRequest request = new CreateLeaveRequest(doctorId, pastStart, pastEnd, "Past leave");

        BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.createLeaveRequest(request));
        assertEquals(ErrorCode.PAST_LEAVE_NOT_ALLOWED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject leave request when startDateTime >= endDateTime")
    void shouldRejectWhenStartDateTimeAfterEndDateTime() {
        CreateLeaveRequest request = new CreateLeaveRequest(doctorId, endDateTime, startDateTime, "Invalid dates");

        BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.createLeaveRequest(request));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject leave request when doctor already has overlapping leave")
    void shouldRejectWhenDoctorHasOverlappingLeave() {
        CreateLeaveRequest request = new CreateLeaveRequest(doctorId, startDateTime, endDateTime, "Overlap test");

        when(leaveRequestRepository.hasOverlappingLeave(doctorId, startDateTime, endDateTime)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.createLeaveRequest(request));
        assertEquals(ErrorCode.LEAVE_REQUEST_OVERLAP, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should approve leave request successfully without affected schedules")
    void shouldApproveLeaveRequestSuccessfullyWithoutSchedules() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .status(LeaveRequestStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workScheduleRepository.findApprovedSchedulesInDateRange(doctorId, startDateTime.toLocalDate(), endDateTime.toLocalDate()))
                .thenReturn(List.of());

        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest("admin-user", "Approved by clinic director");
        ApprovedLeaveResult result = leaveRequestService.approveLeaveRequest(leaveRequestId, reviewRequest);

        assertNotNull(result);
        assertEquals(LeaveRequestStatus.APPROVED, result.leaveRequest().status());
        assertEquals("admin-user", result.leaveRequest().reviewedBy());
        assertNotNull(result.leaveRequest().reviewedAt());
        assertEquals(0, result.cancelledSchedulesCount());
        assertEquals(0, result.schedulesWithAppointmentsCount());
    }

    @Test
    @DisplayName("Case A: Should cancel schedule and block slots when schedule has NO appointments (BR-LEAVE-004)")
    void shouldCancelScheduleWhenNoAppointments_CaseA() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .status(LeaveRequestStatus.PENDING)
                .build();

        UUID scheduleId = UUID.randomUUID();
        LocalDate scheduleDate = startDateTime.toLocalDate().plusDays(1);
        WorkSchedule schedule = WorkSchedule.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .clinic(testClinic)
                .room(testRoom)
                .specialty(testSpecialty)
                .date(scheduleDate)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .status(WorkScheduleStatus.APPROVED)
                .build();

        Slot availableSlot1 = Slot.builder().slotId(UUID.randomUUID()).workSchedule(schedule).status(SlotStatus.AVAILABLE).build();
        Slot availableSlot2 = Slot.builder().slotId(UUID.randomUUID()).workSchedule(schedule).status(SlotStatus.AVAILABLE).build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workScheduleRepository.findApprovedSchedulesInDateRange(doctorId, startDateTime.toLocalDate(), endDateTime.toLocalDate()))
                .thenReturn(List.of(schedule));
        when(slotRepository.findByWorkSchedule_ScheduleId(scheduleId))
                .thenReturn(List.of(availableSlot1, availableSlot2));

        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest("admin-user", "Approved");
        ApprovedLeaveResult result = leaveRequestService.approveLeaveRequest(leaveRequestId, reviewRequest);

        assertNotNull(result);
        assertEquals(1, result.cancelledSchedulesCount());
        assertEquals(0, result.schedulesWithAppointmentsCount());
        assertEquals(0, result.affectedAppointmentsCount());

        // Verify schedule was cancelled
        assertEquals(WorkScheduleStatus.CANCELLED, schedule.getStatus());
        verify(workScheduleRepository).save(schedule);

        // Verify slots were blocked
        assertEquals(SlotStatus.BLOCKED, availableSlot1.getStatus());
        assertEquals(SlotStatus.BLOCKED, availableSlot2.getStatus());
    }

    @Test
    @DisplayName("Case B: Should preserve schedule and appointments when appointments exist (BR-LEAVE-005)")
    void shouldPreserveScheduleWhenAppointmentsExist_CaseB() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .status(LeaveRequestStatus.PENDING)
                .build();

        UUID scheduleId = UUID.randomUUID();
        LocalDate scheduleDate = startDateTime.toLocalDate().plusDays(1);
        WorkSchedule schedule = WorkSchedule.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .clinic(testClinic)
                .room(testRoom)
                .specialty(testSpecialty)
                .date(scheduleDate)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .status(WorkScheduleStatus.APPROVED)
                .build();

        UUID bookedSlotId = UUID.randomUUID();
        Slot bookedSlot = Slot.builder().slotId(bookedSlotId).workSchedule(schedule).status(SlotStatus.BOOKED).build();
        Slot availableSlot = Slot.builder().slotId(UUID.randomUUID()).workSchedule(schedule).status(SlotStatus.AVAILABLE).build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workScheduleRepository.findApprovedSchedulesInDateRange(doctorId, startDateTime.toLocalDate(), endDateTime.toLocalDate()))
                .thenReturn(List.of(schedule));
        when(slotRepository.findByWorkSchedule_ScheduleId(scheduleId))
                .thenReturn(List.of(bookedSlot, availableSlot));

        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest("admin-user", "Approved");
        ApprovedLeaveResult result = leaveRequestService.approveLeaveRequest(leaveRequestId, reviewRequest);

        assertNotNull(result);
        assertEquals(0, result.cancelledSchedulesCount());
        assertEquals(1, result.schedulesWithAppointmentsCount());
        assertEquals(1, result.affectedAppointmentsCount());

        // BR-LEAVE-005: Schedule must NOT be cancelled!
        assertEquals(WorkScheduleStatus.APPROVED, schedule.getStatus());
        verify(workScheduleRepository, never()).save(schedule);

        // Verify affected booked slots list
        assertEquals(List.of(bookedSlotId), result.affectedSchedules().get(0).bookedSlotIds());
        assertEquals("PRESERVED_HAS_APPOINTMENTS", result.affectedSchedules().get(0).handlingType());
        verify(eventPublisher).publish(argThat(event -> "LeaveRequest.Approved".equals(event.eventType())));
    }

    @Test
    @DisplayName("Should handle mixed Case A and Case B schedules accurately")
    void shouldHandleMixedSchedules_CaseA_and_CaseB() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .status(LeaveRequestStatus.PENDING)
                .build();

        // Schedule 1: No appointments (Case A)
        UUID scheduleId1 = UUID.randomUUID();
        WorkSchedule schedule1 = WorkSchedule.builder()
                .scheduleId(scheduleId1)
                .doctorId(doctorId)
                .clinic(testClinic)
                .room(testRoom)
                .specialty(testSpecialty)
                .date(startDateTime.toLocalDate().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .status(WorkScheduleStatus.APPROVED)
                .build();
        Slot slot1 = Slot.builder().slotId(UUID.randomUUID()).workSchedule(schedule1).status(SlotStatus.AVAILABLE).build();

        // Schedule 2: Has appointments (Case B)
        UUID scheduleId2 = UUID.randomUUID();
        WorkSchedule schedule2 = WorkSchedule.builder()
                .scheduleId(scheduleId2)
                .doctorId(doctorId)
                .clinic(testClinic)
                .room(testRoom)
                .specialty(testSpecialty)
                .date(startDateTime.toLocalDate().plusDays(2))
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(17, 0))
                .status(WorkScheduleStatus.APPROVED)
                .build();
        Slot slot2Booked = Slot.builder().slotId(UUID.randomUUID()).workSchedule(schedule2).status(SlotStatus.BOOKED).build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workScheduleRepository.findApprovedSchedulesInDateRange(doctorId, startDateTime.toLocalDate(), endDateTime.toLocalDate()))
                .thenReturn(List.of(schedule1, schedule2));
        when(slotRepository.findByWorkSchedule_ScheduleId(scheduleId1)).thenReturn(List.of(slot1));
        when(slotRepository.findByWorkSchedule_ScheduleId(scheduleId2)).thenReturn(List.of(slot2Booked));

        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest("admin-user", "Approved");
        ApprovedLeaveResult result = leaveRequestService.approveLeaveRequest(leaveRequestId, reviewRequest);

        assertNotNull(result);
        assertEquals(1, result.cancelledSchedulesCount());
        assertEquals(1, result.schedulesWithAppointmentsCount());
        assertEquals(1, result.affectedAppointmentsCount());

        assertEquals(WorkScheduleStatus.CANCELLED, schedule1.getStatus());
        assertEquals(WorkScheduleStatus.APPROVED, schedule2.getStatus());
    }

    @Test
    @DisplayName("Should query affected schedules without mutating state")
    void shouldGetAffectedSchedulesQuery() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .status(LeaveRequestStatus.PENDING)
                .build();

        UUID scheduleId = UUID.randomUUID();
        WorkSchedule schedule = WorkSchedule.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .clinic(testClinic)
                .room(testRoom)
                .specialty(testSpecialty)
                .date(startDateTime.toLocalDate().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .status(WorkScheduleStatus.APPROVED)
                .build();
        Slot slot1 = Slot.builder().slotId(UUID.randomUUID()).workSchedule(schedule).status(SlotStatus.AVAILABLE).build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));
        when(workScheduleRepository.findApprovedSchedulesInDateRange(doctorId, startDateTime.toLocalDate(), endDateTime.toLocalDate()))
                .thenReturn(List.of(schedule));
        when(slotRepository.findByWorkSchedule_ScheduleId(scheduleId)).thenReturn(List.of(slot1));

        ApprovedLeaveResult result = leaveRequestService.getAffectedSchedules(leaveRequestId);

        assertNotNull(result);
        assertEquals(1, result.cancelledSchedulesCount());
        // Verify no saves were called since this is a read-only query
        verify(workScheduleRepository, never()).save(any());
        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject self-approval by doctor (BR-LEAVE-003)")
    void shouldRejectSelfApprovalByDoctor() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .status(LeaveRequestStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));

        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest(doctorId.toString(), "Self approve");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> leaveRequestService.approveLeaveRequest(leaveRequestId, reviewRequest));
        assertEquals(ErrorCode.DOCTOR_CANNOT_APPROVE_OWN_LEAVE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject approving when leave request is not PENDING")
    void shouldRejectApproveWhenNotPending() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .status(LeaveRequestStatus.APPROVED)
                .build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));

        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest("admin-user", "Re-approve");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> leaveRequestService.approveLeaveRequest(leaveRequestId, reviewRequest));
        assertEquals(ErrorCode.INVALID_LEAVE_STATUS_TRANSITION, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject leave request successfully")
    void shouldRejectLeaveRequestSuccessfully() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .status(LeaveRequestStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest("admin-user", "Too many doctors on leave");
        LeaveRequestResponse response = leaveRequestService.rejectLeaveRequest(leaveRequestId, reviewRequest);

        assertNotNull(response);
        assertEquals(LeaveRequestStatus.REJECTED, response.status());
        assertEquals("admin-user", response.reviewedBy());
    }

    @Test
    @DisplayName("Should cancel pending leave request successfully")
    void shouldCancelLeaveRequestSuccessfully() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .status(LeaveRequestStatus.PENDING)
                .build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponse response = leaveRequestService.cancelLeaveRequest(leaveRequestId);

        assertNotNull(response);
        assertEquals(LeaveRequestStatus.CANCELLED, response.status());
    }

    @Test
    @DisplayName("Should reject cancelling when leave request is already approved")
    void shouldRejectCancelWhenAlreadyApproved() {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .leaveRequestId(leaveRequestId)
                .doctorId(doctorId)
                .status(LeaveRequestStatus.APPROVED)
                .build();

        when(leaveRequestRepository.findById(leaveRequestId)).thenReturn(Optional.of(leaveRequest));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> leaveRequestService.cancelLeaveRequest(leaveRequestId));
        assertEquals(ErrorCode.INVALID_LEAVE_STATUS_TRANSITION, ex.getErrorCode());
    }
}
