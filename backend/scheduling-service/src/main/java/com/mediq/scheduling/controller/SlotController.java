package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.response.SlotResponse;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @GetMapping("/{id}")
    public ResponseEntity<SlotResponse> getSlotById(@PathVariable("id") UUID slotId) {
        return ResponseEntity.ok(slotService.getSlotById(slotId));
    }

    @GetMapping
    public ResponseEntity<List<SlotResponse>> findSlots(
            @RequestParam(value = "doctorId", required = false) UUID doctorId,
            @RequestParam(value = "clinicId", required = false) UUID clinicId,
            @RequestParam(value = "specialtyId", required = false) UUID specialtyId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) SlotStatus status
    ) {
        return ResponseEntity.ok(slotService.findSlots(doctorId, clinicId, specialtyId, date, status));
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<SlotResponse> holdSlot(@PathVariable("id") UUID slotId) {
        return ResponseEntity.ok(slotService.holdSlot(slotId));
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<SlotResponse> bookSlot(@PathVariable("id") UUID slotId) {
        return ResponseEntity.ok(slotService.bookSlot(slotId));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<SlotResponse> releaseSlot(@PathVariable("id") UUID slotId) {
        return ResponseEntity.ok(slotService.releaseSlot(slotId));
    }

    @PostMapping("/{id}/cancel-booking")
    public ResponseEntity<SlotResponse> cancelBooking(@PathVariable("id") UUID slotId) {
        return ResponseEntity.ok(slotService.cancelBooking(slotId));
    }
}
