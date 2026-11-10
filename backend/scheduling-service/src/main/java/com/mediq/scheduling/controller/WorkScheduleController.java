package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.request.CreateWorkScheduleRequest;
import com.mediq.scheduling.dto.response.WorkScheduleResponse;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.service.WorkScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    @PostMapping
    public ResponseEntity<WorkScheduleResponse> createWorkSchedule(@RequestBody CreateWorkScheduleRequest request) {
        WorkScheduleResponse response = workScheduleService.createWorkSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkScheduleResponse> getWorkScheduleById(@PathVariable("id") UUID scheduleId) {
        return ResponseEntity.ok(workScheduleService.getWorkScheduleById(scheduleId));
    }

    @GetMapping
    public ResponseEntity<List<WorkScheduleResponse>> listWorkSchedules(
            @RequestParam(value = "doctorId", required = false) UUID doctorId,
            @RequestParam(value = "clinicId", required = false) UUID clinicId,
            @RequestParam(value = "roomId", required = false) UUID roomId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "status", required = false) WorkScheduleStatus status
    ) {
        return ResponseEntity.ok(workScheduleService.getWorkSchedules(doctorId, clinicId, roomId, date, status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<WorkScheduleResponse> approveWorkSchedule(@PathVariable("id") UUID scheduleId) {
        return ResponseEntity.ok(workScheduleService.approveWorkSchedule(scheduleId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<WorkScheduleResponse> cancelWorkSchedule(@PathVariable("id") UUID scheduleId) {
        return ResponseEntity.ok(workScheduleService.cancelWorkSchedule(scheduleId));
    }
}
