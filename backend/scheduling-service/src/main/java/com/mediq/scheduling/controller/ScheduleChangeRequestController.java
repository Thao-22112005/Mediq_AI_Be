package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.request.CreateScheduleChangeRequest;
import com.mediq.scheduling.dto.request.ReviewScheduleChangeRequest;
import com.mediq.scheduling.dto.response.ScheduleChangeRequestResponse;
import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;
import com.mediq.scheduling.service.ScheduleChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedule-change-requests")
@RequiredArgsConstructor
public class ScheduleChangeRequestController {

    private final ScheduleChangeRequestService scheduleChangeRequestService;

    @PostMapping
    public ResponseEntity<ScheduleChangeRequestResponse> createRequest(@RequestBody CreateScheduleChangeRequest request) {
        ScheduleChangeRequestResponse response = scheduleChangeRequestService.createRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleChangeRequestResponse> getRequestById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(scheduleChangeRequestService.getRequestById(id));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleChangeRequestResponse>> listRequests(
            @RequestParam(value = "doctorId", required = false) UUID doctorId,
            @RequestParam(value = "status", required = false) ScheduleChangeRequestStatus status
    ) {
        return ResponseEntity.ok(scheduleChangeRequestService.listRequests(doctorId, status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ScheduleChangeRequestResponse> approveRequest(
            @PathVariable("id") UUID id,
            @RequestBody ReviewScheduleChangeRequest request
    ) {
        return ResponseEntity.ok(scheduleChangeRequestService.approveRequest(id, request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ScheduleChangeRequestResponse> rejectRequest(
            @PathVariable("id") UUID id,
            @RequestBody ReviewScheduleChangeRequest request
    ) {
        return ResponseEntity.ok(scheduleChangeRequestService.rejectRequest(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ScheduleChangeRequestResponse> cancelRequest(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(scheduleChangeRequestService.cancelRequest(id));
    }
}
