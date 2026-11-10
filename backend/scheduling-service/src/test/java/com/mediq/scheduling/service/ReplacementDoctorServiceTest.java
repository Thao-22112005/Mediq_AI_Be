package com.mediq.scheduling.service;

import com.mediq.scheduling.client.DoctorClient;
import com.mediq.scheduling.dto.request.FindReplacementCandidatesRequest;
import com.mediq.scheduling.dto.response.ReplacementCandidateDto;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.repository.LeaveRequestRepository;
import com.mediq.scheduling.repository.SlotRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplacementDoctorServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private DoctorClient doctorClient;

    private ReplacementDoctorServiceImpl replacementDoctorService;

    private UUID originalDoctorId;
    private UUID candidateDoctorId1;
    private UUID candidateDoctorId2;
    private UUID specialtyId;
    private UUID mainClinicId;
    private UUID branchClinicId;
    private LocalDate searchDate;
    private LocalTime searchTime;

    private Clinic mainClinic;
    private Clinic branchClinic;
    private Specialty specialty;
    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        replacementDoctorService = new ReplacementDoctorServiceImpl(
                slotRepository,
                leaveRequestRepository,
                doctorClient
        );

        originalDoctorId = UUID.randomUUID();
        candidateDoctorId1 = UUID.randomUUID();
        candidateDoctorId2 = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
        mainClinicId = UUID.randomUUID();
        branchClinicId = UUID.randomUUID();
        searchDate = LocalDate.now().plusDays(2);
        searchTime = LocalTime.of(9, 0);

        mainClinic = Clinic.builder().clinicId(mainClinicId).name("Main Clinic").build();
        branchClinic = Clinic.builder().clinicId(branchClinicId).name("Branch Clinic").build();
        specialty = Specialty.builder().specialtyId(specialtyId).name("Cardiology").build();
        room1 = Room.builder().roomId(UUID.randomUUID()).code("101").name("Room 101").build();
        room2 = Room.builder().roomId(UUID.randomUUID()).code("201").name("Room 201").build();
    }

    @Test
    @DisplayName("Should find replacement doctor in same clinic with Priority 1 (BR-LEAVE-006)")
    void shouldFindReplacementDoctorSameClinic_Priority1() {
        WorkSchedule ws = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(candidateDoctorId1)
                .clinic(mainClinic)
                .room(room1)
                .specialty(specialty)
                .date(searchDate)
                .build();

        Slot availableSlot = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(ws)
                .startTime(searchTime)
                .endTime(searchTime.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findSlotsWithFilter(null, null, specialtyId, searchDate, SlotStatus.AVAILABLE))
                .thenReturn(List.of(availableSlot));
        when(doctorClient.isDoctorActive(candidateDoctorId1)).thenReturn(true);
        when(doctorClient.getDoctor(candidateDoctorId1))
                .thenReturn(Optional.of(new DoctorClient.DoctorDto(candidateDoctorId1, "Dr. Alice", "ACTIVE")));
        when(leaveRequestRepository.hasOverlappingLeave(eq(candidateDoctorId1), any(), any()))
                .thenReturn(false);

        FindReplacementCandidatesRequest request = FindReplacementCandidatesRequest.builder()
                .originalDoctorId(originalDoctorId)
                .specialtyId(specialtyId)
                .date(searchDate)
                .startTime(searchTime)
                .currentClinicId(mainClinicId)
                .build();

        List<ReplacementCandidateDto> result = replacementDoctorService.findReplacementCandidates(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        ReplacementCandidateDto candidate = result.get(0);
        assertEquals(candidateDoctorId1, candidate.doctorId());
        assertEquals("Dr. Alice", candidate.doctorName());
        assertEquals(mainClinicId, candidate.clinicId());
        assertFalse(candidate.isCrossClinic());
        assertEquals(1, candidate.priority());
    }

    @Test
    @DisplayName("Should find replacement doctor in another clinic with Priority 2 (BR-LEAVE-007 Cross-Clinic)")
    void shouldFindReplacementDoctorCrossClinic_Priority2() {
        WorkSchedule ws = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(candidateDoctorId2)
                .clinic(branchClinic)
                .room(room2)
                .specialty(specialty)
                .date(searchDate)
                .build();

        Slot availableSlot = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(ws)
                .startTime(searchTime)
                .endTime(searchTime.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findSlotsWithFilter(null, null, specialtyId, searchDate, SlotStatus.AVAILABLE))
                .thenReturn(List.of(availableSlot));
        when(doctorClient.isDoctorActive(candidateDoctorId2)).thenReturn(true);
        when(doctorClient.getDoctor(candidateDoctorId2))
                .thenReturn(Optional.of(new DoctorClient.DoctorDto(candidateDoctorId2, "Dr. Bob", "ACTIVE")));
        when(leaveRequestRepository.hasOverlappingLeave(eq(candidateDoctorId2), any(), any()))
                .thenReturn(false);

        FindReplacementCandidatesRequest request = FindReplacementCandidatesRequest.builder()
                .originalDoctorId(originalDoctorId)
                .specialtyId(specialtyId)
                .date(searchDate)
                .startTime(searchTime)
                .currentClinicId(mainClinicId)
                .build();

        List<ReplacementCandidateDto> result = replacementDoctorService.findReplacementCandidates(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        ReplacementCandidateDto candidate = result.get(0);
        assertEquals(candidateDoctorId2, candidate.doctorId());
        assertEquals(branchClinicId, candidate.clinicId());
        assertTrue(candidate.isCrossClinic());
        assertEquals(2, candidate.priority());
    }

    @Test
    @DisplayName("Should exclude candidate if slot belongs to the original doctor on leave")
    void shouldExcludeOriginalDoctorOnLeave() {
        WorkSchedule ws = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(originalDoctorId) // Same as original doctor!
                .clinic(mainClinic)
                .room(room1)
                .specialty(specialty)
                .date(searchDate)
                .build();

        Slot availableSlot = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(ws)
                .startTime(searchTime)
                .endTime(searchTime.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findSlotsWithFilter(null, null, specialtyId, searchDate, SlotStatus.AVAILABLE))
                .thenReturn(List.of(availableSlot));

        FindReplacementCandidatesRequest request = FindReplacementCandidatesRequest.builder()
                .originalDoctorId(originalDoctorId)
                .specialtyId(specialtyId)
                .date(searchDate)
                .startTime(searchTime)
                .currentClinicId(mainClinicId)
                .build();

        List<ReplacementCandidateDto> result = replacementDoctorService.findReplacementCandidates(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should exclude candidate if doctor is not ACTIVE (WF-04)")
    void shouldExcludeInactiveDoctor() {
        WorkSchedule ws = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(candidateDoctorId1)
                .clinic(mainClinic)
                .room(room1)
                .specialty(specialty)
                .date(searchDate)
                .build();

        Slot availableSlot = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(ws)
                .startTime(searchTime)
                .endTime(searchTime.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findSlotsWithFilter(null, null, specialtyId, searchDate, SlotStatus.AVAILABLE))
                .thenReturn(List.of(availableSlot));
        when(doctorClient.isDoctorActive(candidateDoctorId1)).thenReturn(false); // Inactive!

        FindReplacementCandidatesRequest request = FindReplacementCandidatesRequest.builder()
                .originalDoctorId(originalDoctorId)
                .specialtyId(specialtyId)
                .date(searchDate)
                .startTime(searchTime)
                .currentClinicId(mainClinicId)
                .build();

        List<ReplacementCandidateDto> result = replacementDoctorService.findReplacementCandidates(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should exclude candidate if doctor has overlapping leave request (WF-04)")
    void shouldExcludeDoctorWithOverlappingLeave() {
        WorkSchedule ws = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(candidateDoctorId1)
                .clinic(mainClinic)
                .room(room1)
                .specialty(specialty)
                .date(searchDate)
                .build();

        Slot availableSlot = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(ws)
                .startTime(searchTime)
                .endTime(searchTime.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findSlotsWithFilter(null, null, specialtyId, searchDate, SlotStatus.AVAILABLE))
                .thenReturn(List.of(availableSlot));
        when(doctorClient.isDoctorActive(candidateDoctorId1)).thenReturn(true);
        when(leaveRequestRepository.hasOverlappingLeave(eq(candidateDoctorId1), any(), any()))
                .thenReturn(true); // Candidate doctor is also on leave!

        FindReplacementCandidatesRequest request = FindReplacementCandidatesRequest.builder()
                .originalDoctorId(originalDoctorId)
                .specialtyId(specialtyId)
                .date(searchDate)
                .startTime(searchTime)
                .currentClinicId(mainClinicId)
                .build();

        List<ReplacementCandidateDto> result = replacementDoctorService.findReplacementCandidates(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should prioritize same-clinic candidates ahead of cross-clinic candidates")
    void shouldPrioritizeSameClinicOverCrossClinic() {
        // Candidate 1: Same clinic
        WorkSchedule ws1 = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(candidateDoctorId1)
                .clinic(mainClinic)
                .room(room1)
                .specialty(specialty)
                .date(searchDate)
                .build();
        Slot slot1 = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(ws1)
                .startTime(searchTime)
                .endTime(searchTime.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        // Candidate 2: Branch clinic (cross-clinic)
        WorkSchedule ws2 = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(candidateDoctorId2)
                .clinic(branchClinic)
                .room(room2)
                .specialty(specialty)
                .date(searchDate)
                .build();
        Slot slot2 = Slot.builder()
                .slotId(UUID.randomUUID())
                .workSchedule(ws2)
                .startTime(searchTime)
                .endTime(searchTime.plusMinutes(30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findSlotsWithFilter(null, null, specialtyId, searchDate, SlotStatus.AVAILABLE))
                .thenReturn(List.of(slot2, slot1)); // Intentionally passed in reverse order
        when(doctorClient.isDoctorActive(candidateDoctorId1)).thenReturn(true);
        when(doctorClient.isDoctorActive(candidateDoctorId2)).thenReturn(true);
        when(doctorClient.getDoctor(candidateDoctorId1))
                .thenReturn(Optional.of(new DoctorClient.DoctorDto(candidateDoctorId1, "Dr. Alice", "ACTIVE")));
        when(doctorClient.getDoctor(candidateDoctorId2))
                .thenReturn(Optional.of(new DoctorClient.DoctorDto(candidateDoctorId2, "Dr. Bob", "ACTIVE")));
        when(leaveRequestRepository.hasOverlappingLeave(any(), any(), any())).thenReturn(false);

        FindReplacementCandidatesRequest request = FindReplacementCandidatesRequest.builder()
                .originalDoctorId(originalDoctorId)
                .specialtyId(specialtyId)
                .date(searchDate)
                .startTime(searchTime)
                .currentClinicId(mainClinicId)
                .build();

        List<ReplacementCandidateDto> result = replacementDoctorService.findReplacementCandidates(request);

        assertNotNull(result);
        assertEquals(2, result.size());
        // Priority 1 (same clinic) must come first!
        assertEquals(1, result.get(0).priority());
        assertEquals(candidateDoctorId1, result.get(0).doctorId());
        assertFalse(result.get(0).isCrossClinic());

        // Priority 2 (cross clinic) must come second!
        assertEquals(2, result.get(1).priority());
        assertEquals(candidateDoctorId2, result.get(1).doctorId());
        assertTrue(result.get(1).isCrossClinic());
    }

    @Test
    @DisplayName("Should reject search when required parameters are missing")
    void shouldRejectWhenRequiredParametersMissing() {
        FindReplacementCandidatesRequest invalidRequest = FindReplacementCandidatesRequest.builder()
                .specialtyId(null)
                .build();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> replacementDoctorService.findReplacementCandidates(invalidRequest));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }
}
