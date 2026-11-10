package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.request.CreateLeaveRequest;
import com.mediq.scheduling.dto.request.ReviewLeaveRequest;
import com.mediq.scheduling.dto.response.LeaveRequestResponse;
import com.mediq.scheduling.entity.LeaveRequestStatus;
import com.mediq.scheduling.service.LeaveRequestService;
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
@RequestMapping("/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> createLeaveRequest(@RequestBody CreateLeaveRequest request) {
        LeaveRequestResponse response = leaveRequestService.createLeaveRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestResponse> getLeaveRequestById(@PathVariable("id") UUID leaveRequestId) {
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestById(leaveRequestId));
    }

    @GetMapping("/{id}/affected-schedules")
    public ResponseEntity<com.mediq.scheduling.dto.response.ApprovedLeaveResult> getAffectedSchedules(
            @PathVariable("id") UUID leaveRequestId
    ) {
        return ResponseEntity.ok(leaveRequestService.getAffectedSchedules(leaveRequestId));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> listLeaveRequests(
            @RequestParam(value = "doctorId", required = false) UUID doctorId,
            @RequestParam(value = "status", required = false) LeaveRequestStatus status
    ) {
        return ResponseEntity.ok(leaveRequestService.listLeaveRequests(doctorId, status));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<com.mediq.scheduling.dto.response.ApprovedLeaveResult> approveLeaveRequest(
            @PathVariable("id") UUID leaveRequestId,
            @RequestBody ReviewLeaveRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.approveLeaveRequest(leaveRequestId, request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> rejectLeaveRequest(
            @PathVariable("id") UUID leaveRequestId,
            @RequestBody ReviewLeaveRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.rejectLeaveRequest(leaveRequestId, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequestResponse> cancelLeaveRequest(@PathVariable("id") UUID leaveRequestId) {
        return ResponseEntity.ok(leaveRequestService.cancelLeaveRequest(leaveRequestId));
    }
}
