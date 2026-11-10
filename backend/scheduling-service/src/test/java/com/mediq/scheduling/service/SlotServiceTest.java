package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.response.SlotResponse;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.SlotMapper;
import com.mediq.scheduling.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private SlotMapper slotMapper;

    private SlotServiceImpl slotService;

    private WorkSchedule workSchedule;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        slotService = new SlotServiceImpl(slotRepository, slotMapper);

        specialty = Specialty.builder()
                .specialtyId(UUID.randomUUID())
                .name("Cardiology")
                .defaultSlotDuration(30)
                .build();

        Clinic clinic = Clinic.builder()
                .clinicId(UUID.randomUUID())
                .code("CLN-01")
                .build();

        Room room = Room.builder()
                .roomId(UUID.randomUUID())
                .code("R-101")
                .build();

        workSchedule = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .clinic(clinic)
                .specialty(specialty)
                .room(room)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .status(WorkScheduleStatus.APPROVED)
                .build();
    }

    @Test
    @DisplayName("Should generate slots correctly based on specialty duration (WF-01, BR-SLOT-001)")
    void shouldGenerateSlotsForSchedule() {
        when(slotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        slotService.generateSlotsForSchedule(workSchedule);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Slot>> captor = ArgumentCaptor.forClass(List.class);
        verify(slotRepository).saveAll(captor.capture());

        List<Slot> savedSlots = captor.getValue();
        assertEquals(2, savedSlots.size()); // 8:00 - 8:30, 8:30 - 9:00

        Slot slot1 = savedSlots.get(0);
        assertEquals(LocalTime.of(8, 0), slot1.getStartTime());
        assertEquals(LocalTime.of(8, 30), slot1.getEndTime());
        assertEquals(SlotStatus.AVAILABLE, slot1.getStatus());

        Slot slot2 = savedSlots.get(1);
        assertEquals(LocalTime.of(8, 30), slot2.getStartTime());
        assertEquals(LocalTime.of(9, 0), slot2.getEndTime());
        assertEquals(SlotStatus.AVAILABLE, slot2.getStatus());
    }

    @Test
    @DisplayName("Should hold slot successfully from AVAILABLE to HELD")
    void shouldHoldSlotSuccessfully() {
        UUID slotId = UUID.randomUUID();
        Slot slot = Slot.builder()
                .slotId(slotId)
                .workSchedule(workSchedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findByIdWithLock(slotId)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(Slot.class))).thenReturn(slot);

        SlotResponse expectedResponse = new SlotResponse(
                slotId, workSchedule.getScheduleId(), workSchedule.getDoctorId(),
                workSchedule.getClinic().getClinicId(), specialty.getSpecialtyId(),
                workSchedule.getRoom().getRoomId(), workSchedule.getDate(),
                LocalTime.of(8, 0), LocalTime.of(8, 30), SlotStatus.HELD,
                Instant.now(), Instant.now()
        );
        when(slotMapper.toSlotResponse(slot)).thenReturn(expectedResponse);

        SlotResponse response = slotService.holdSlot(slotId);

        assertNotNull(response);
        assertEquals(SlotStatus.HELD, response.status());
        verify(slotRepository).save(slot);
    }

    @Test
    @DisplayName("Should reject hold when slot is not AVAILABLE (SLOT_NOT_AVAILABLE)")
    void shouldRejectHoldWhenSlotNotAvailable() {
        UUID slotId = UUID.randomUUID();
        Slot slot = Slot.builder()
                .slotId(slotId)
                .workSchedule(workSchedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.HELD)
                .build();

        when(slotRepository.findByIdWithLock(slotId)).thenReturn(Optional.of(slot));

        BusinessException ex = assertThrows(BusinessException.class, () -> slotService.holdSlot(slotId));
        assertEquals(ErrorCode.SLOT_NOT_AVAILABLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject hold or book when slot is in the past (BR-SLOT-002)")
    void shouldRejectWhenSlotInPast() {
        WorkSchedule pastSchedule = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .date(LocalDate.now().minusDays(1))
                .build();

        UUID slotId = UUID.randomUUID();
        Slot pastSlot = Slot.builder()
                .slotId(slotId)
                .workSchedule(pastSchedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findByIdWithLock(slotId)).thenReturn(Optional.of(pastSlot));

        BusinessException ex = assertThrows(BusinessException.class, () -> slotService.holdSlot(slotId));
        assertEquals(ErrorCode.PAST_SLOT_NOT_ALLOWED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should book slot successfully from HELD to BOOKED")
    void shouldBookSlotSuccessfully() {
        UUID slotId = UUID.randomUUID();
        Slot slot = Slot.builder()
                .slotId(slotId)
                .workSchedule(workSchedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.HELD)
                .build();

        when(slotRepository.findByIdWithLock(slotId)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(Slot.class))).thenReturn(slot);

        SlotResponse expectedResponse = new SlotResponse(
                slotId, workSchedule.getScheduleId(), workSchedule.getDoctorId(),
                workSchedule.getClinic().getClinicId(), specialty.getSpecialtyId(),
                workSchedule.getRoom().getRoomId(), workSchedule.getDate(),
                LocalTime.of(8, 0), LocalTime.of(8, 30), SlotStatus.BOOKED,
                Instant.now(), Instant.now()
        );
        when(slotMapper.toSlotResponse(slot)).thenReturn(expectedResponse);

        SlotResponse response = slotService.bookSlot(slotId);

        assertNotNull(response);
        assertEquals(SlotStatus.BOOKED, response.status());
    }

    @Test
    @DisplayName("Should reject double booking attempt (SLOT_ALREADY_BOOKED)")
    void shouldRejectDoubleBooking() {
        UUID slotId = UUID.randomUUID();
        Slot slot = Slot.builder()
                .slotId(slotId)
                .workSchedule(workSchedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.BOOKED)
                .build();

        when(slotRepository.findByIdWithLock(slotId)).thenReturn(Optional.of(slot));

        BusinessException ex = assertThrows(BusinessException.class, () -> slotService.bookSlot(slotId));
        assertEquals(ErrorCode.SLOT_ALREADY_BOOKED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should release slot from HELD to AVAILABLE")
    void shouldReleaseSlotSuccessfully() {
        UUID slotId = UUID.randomUUID();
        Slot slot = Slot.builder()
                .slotId(slotId)
                .workSchedule(workSchedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.HELD)
                .build();

        when(slotRepository.findByIdWithLock(slotId)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(Slot.class))).thenReturn(slot);

        SlotResponse expectedResponse = new SlotResponse(
                slotId, workSchedule.getScheduleId(), workSchedule.getDoctorId(),
                workSchedule.getClinic().getClinicId(), specialty.getSpecialtyId(),
                workSchedule.getRoom().getRoomId(), workSchedule.getDate(),
                LocalTime.of(8, 0), LocalTime.of(8, 30), SlotStatus.AVAILABLE,
                Instant.now(), Instant.now()
        );
        when(slotMapper.toSlotResponse(slot)).thenReturn(expectedResponse);

        SlotResponse response = slotService.releaseSlot(slotId);

        assertNotNull(response);
        assertEquals(SlotStatus.AVAILABLE, response.status());
    }

    @Test
    @DisplayName("Should reject release when slot is not HELD")
    void shouldRejectReleaseWhenNotHeld() {
        UUID slotId = UUID.randomUUID();
        Slot slot = Slot.builder()
                .slotId(slotId)
                .workSchedule(workSchedule)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(8, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        when(slotRepository.findByIdWithLock(slotId)).thenReturn(Optional.of(slot));

        BusinessException ex = assertThrows(BusinessException.class, () -> slotService.releaseSlot(slotId));
        assertEquals(ErrorCode.INVALID_SLOT_STATUS_TRANSITION, ex.getErrorCode());
    }
}
