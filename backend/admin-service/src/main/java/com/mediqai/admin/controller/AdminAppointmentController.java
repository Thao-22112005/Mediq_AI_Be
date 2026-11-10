package com.mediqai.admin.controller;

import com.mediqai.admin.dto.response.AdminAppointmentResponse;
import com.mediqai.admin.service.AdminAppointmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    private final AdminAppointmentService appointmentService;

    public AdminAppointmentController(
            AdminAppointmentService appointmentService
    ) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AdminAppointmentResponse> getAppointments(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        String token = extractToken(authorization);

        return appointmentService.getAppointments(
                date, status, doctorId, patientId, token
        );
    }

    @GetMapping("/{id}")
    public AdminAppointmentResponse getAppointmentById(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        String token = extractToken(authorization);

        return appointmentService.getAppointmentById(id, token);
    }

    private String extractToken(String authorization) {
        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.length() <= 7) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token không hợp lệ"
            );
        }

        return authorization.substring(7);
    }
}