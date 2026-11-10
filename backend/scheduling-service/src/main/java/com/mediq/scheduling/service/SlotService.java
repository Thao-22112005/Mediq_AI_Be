package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.response.SlotResponse;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.WorkSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SlotService {

    List<SlotResponse> generateSlotsForSchedule(WorkSchedule schedule);

    SlotResponse getSlotById(UUID slotId);

    List<SlotResponse> findSlots(
            UUID doctorId,
            UUID clinicId,
            UUID specialtyId,
            LocalDate date,
            SlotStatus status
    );

    SlotResponse holdSlot(UUID slotId);

    SlotResponse bookSlot(UUID slotId);

    SlotResponse releaseSlot(UUID slotId);

    SlotResponse cancelBooking(UUID slotId);
}