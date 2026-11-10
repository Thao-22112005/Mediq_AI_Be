package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.response.SlotResponse;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.SlotMapper;
import com.mediq.scheduling.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlotServiceImpl implements SlotService {

    private final SlotRepository slotRepository;
    private final SlotMapper slotMapper;

    @Override
    @Transactional
    public List<SlotResponse> generateSlotsForSchedule(WorkSchedule schedule) {
        int durationMinutes = (schedule.getSpecialty() != null && schedule.getSpecialty().getDefaultSlotDuration() != null)
                ? schedule.getSpecialty().getDefaultSlotDuration()
                : 15;

        List<Slot> slots = new ArrayList<>();
        LocalTime currentStart = schedule.getStartTime();
        LocalTime scheduleEnd = schedule.getEndTime();

        while (currentStart.plusMinutes(durationMinutes).compareTo(scheduleEnd) <= 0) {
            LocalTime currentEnd = currentStart.plusMinutes(durationMinutes);

            Slot slot = Slot.builder()
                    .slotId(UUID.randomUUID())
                    .workSchedule(schedule)
                    .startTime(currentStart)
                    .endTime(currentEnd)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            slots.add(slot);
            currentStart = currentEnd;
        }

        List<Slot> savedSlots = slotRepository.saveAll(slots);
        return slotMapper.toSlotResponseList(savedSlots);
    }

    @Override
    public SlotResponse getSlotById(UUID slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND,
                        "Slot not found with ID: " + slotId));
        return slotMapper.toSlotResponse(slot);
    }

    @Override
    public List<SlotResponse> findSlots(UUID doctorId, UUID clinicId, UUID specialtyId, LocalDate date, SlotStatus status) {
        List<Slot> slots = slotRepository.findSlotsWithFilter(doctorId, clinicId, specialtyId, date, status);
        return slotMapper.toSlotResponseList(slots);
    }

    @Override
    @Transactional
    public SlotResponse holdSlot(UUID slotId) {
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND,
                        "Slot not found with ID: " + slotId));

        validateNotPastSlot(slot);

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SLOT_NOT_AVAILABLE,
                    "Slot is not available for holding (current status: " + slot.getStatus() + ")");
        }

        slot.setStatus(SlotStatus.HELD);
        Slot updatedSlot = slotRepository.save(slot);
        return slotMapper.toSlotResponse(updatedSlot);
    }

    @Override
    @Transactional
    public SlotResponse bookSlot(UUID slotId) {
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND,
                        "Slot not found with ID: " + slotId));

        validateNotPastSlot(slot);

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED, "Slot is already booked");
        }

        if (slot.getStatus() != SlotStatus.HELD && slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SLOT_NOT_AVAILABLE,
                    "Slot is not in an allowable status to book (current status: " + slot.getStatus() + ")");
        }

        slot.setStatus(SlotStatus.BOOKED);
        Slot updatedSlot = slotRepository.save(slot);
        return slotMapper.toSlotResponse(updatedSlot);
    }

    @Override
    @Transactional
    public SlotResponse releaseSlot(UUID slotId) {
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND,
                        "Slot not found with ID: " + slotId));

        if (slot.getStatus() != SlotStatus.HELD) {
            throw new BusinessException(ErrorCode.INVALID_SLOT_STATUS_TRANSITION,
                    "Only HELD slots can be released back to AVAILABLE (current status: " + slot.getStatus() + ")");
        }

        slot.setStatus(SlotStatus.AVAILABLE);
        Slot updatedSlot = slotRepository.save(slot);
        return slotMapper.toSlotResponse(updatedSlot);
    }

    private void validateNotPastSlot(Slot slot) {
        if (slot.getWorkSchedule() != null && slot.getWorkSchedule().getDate() != null && slot.getStartTime() != null) {
            LocalDateTime slotDateTime = LocalDateTime.of(slot.getWorkSchedule().getDate(), slot.getStartTime());
            if (slotDateTime.isBefore(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.PAST_SLOT_NOT_ALLOWED,
                        "Cannot hold or book a slot in the past (BR-SLOT-002)");
            }
        }
    }

    @Override
    @Transactional
    public SlotResponse cancelBooking(UUID slotId) {
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SLOT_NOT_FOUND,
                        "Slot not found with ID: " + slotId
                ));

        if (slot.getStatus() != SlotStatus.BOOKED) {
            throw new BusinessException(
                    ErrorCode.INVALID_SLOT_STATUS_TRANSITION,
                    "Only BOOKED slots can be cancelled back to AVAILABLE (current status: "
                            + slot.getStatus() + ")"
            );
        }

        slot.setStatus(SlotStatus.AVAILABLE);

        Slot updatedSlot = slotRepository.save(slot);

        return slotMapper.toSlotResponse(updatedSlot);
    }
}
