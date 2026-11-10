package com.mediqai.appointment.controller;

import com.mediqai.appointment.dto.request.CreateAppointmentRequest;
import com.mediqai.appointment.dto.request.RescheduleAppointmentRequest;
import com.mediqai.appointment.dto.request.UpdateAppointmentStatusRequest;
import com.mediqai.appointment.dto.response.AppointmentHistoryResponse;
import com.mediqai.appointment.dto.response.AppointmentResponse;
import com.mediqai.appointment.dto.response.SpecialtyAppointmentStatsResponse;
import com.mediqai.appointment.entity.AppointmentStatus;
import com.mediqai.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Patient đặt lịch khám
     *
     * Patient chọn một Slot AVAILABLE
     * đã được Admin tạo từ trước.
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        AppointmentResponse response =
                appointmentService.createAppointment(request, token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Cập nhật trạng thái lịch hẹn
     *
     * Patient:
     * - Chỉ được CANCELLED
     * - Chỉ được hủy appointment của chính mình
     *
     * Doctor:
     * - PENDING -> CONFIRMED
     * - CONFIRMED -> COMPLETED
     * - CONFIRMED -> NO_SHOW
     *
     * Admin:
     * - Có quyền xử lý trạng thái
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request,
            Authentication authentication,
            @RequestHeader("Authorization") String authorizationHeader) {

        AppointmentStatus newStatus = request.getStatus();

        // =========================================================
        // ADMIN
        // =========================================================

        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {

            return ResponseEntity.ok(
                    appointmentService.updateStatus(id, request)
            );
        }

        // =========================================================
        // PATIENT
        // =========================================================

        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {

            // Patient chỉ được CANCELLED
            if (newStatus != AppointmentStatus.CANCELLED) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .build();
            }

            String token = authorizationHeader.substring(7);

            return ResponseEntity.ok(
                    appointmentService.cancelByPatient(
                            id,
                            request,
                            token
                    )
            );
        }

        // =========================================================
        // DOCTOR
        // =========================================================

        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_DOCTOR"))) {

            boolean allowed =
                    newStatus == AppointmentStatus.CONFIRMED
                            || newStatus == AppointmentStatus.COMPLETED
                            || newStatus == AppointmentStatus.NO_SHOW;

            if (!allowed) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .build();
            }

            return ResponseEntity.ok(
                    appointmentService.updateStatus(id, request)
            );
        }

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }

    /**
     * Patient xem appointment của chính mình
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        return ResponseEntity.ok(
                appointmentService.getMyAppointments(token)
        );
    }

    /**
     * Xem chi tiết lịch hẹn
     *
     * Patient:
     * - Chỉ được xem appointment của chính mình
     *
     * Doctor:
     * - Tạm thời sử dụng logic hiện tại
     *
     * Admin:
     * - Được xem tất cả
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestHeader("Authorization") String authorizationHeader) {

        // PATIENT
        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {

            String token = authorizationHeader.substring(7);

            return ResponseEntity.ok(
                    appointmentService.getPatientAppointmentById(
                            id,
                            token
                    )
            );
        }

        // DOCTOR / ADMIN
        return ResponseEntity.ok(
                appointmentService.getAppointmentById(id)
        );
    }

    /**
     * Xem danh sách lịch hẹn
     *
     * Patient:
     * - Không sử dụng API này để truyền patientId
     * - Sử dụng GET /appointments/my
     *
     * Doctor/Admin:
     * - Có thể sử dụng các filter hiện tại
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID clinicId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) AppointmentStatus status,
            Authentication authentication) {

        // Patient không được tự truyền patientId
        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
                appointmentService.getAppointments(
                        patientId,
                        doctorId,
                        clinicId,
                        date,
                        status
                )
        );
    }

    /**
     * Xem lịch sử appointment
     *
     * Patient:
     * - Chỉ được xem history của appointment của mình
     *
     * Doctor/Admin:
     * - Tạm thời sử dụng logic hiện tại
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentHistoryResponse>> getAppointmentHistory(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestHeader("Authorization") String authorizationHeader) {

        // PATIENT
        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {

            String token = authorizationHeader.substring(7);

            // Kiểm tra ownership trước
            appointmentService.getPatientAppointmentById(
                    id,
                    token
            );
        }

        return ResponseEntity.ok(
                appointmentService.getAppointmentHistory(id)
        );
    }

    /**
     * Patient đổi lịch hẹn của mình
     */
    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        return ResponseEntity.ok(
                appointmentService.rescheduleByPatient(
                        id,
                        request,
                        token
                )
        );
    }

    @GetMapping("/statistics/specialties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SpecialtyAppointmentStatsResponse>>
    getSpecialtyAppointmentStats(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            java.time.YearMonth month
    ) {
        java.time.YearMonth targetMonth =
                month != null ? month : java.time.YearMonth.now();

        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate nextMonthStart = targetMonth.plusMonths(1).atDay(1);

        List<SpecialtyAppointmentStatsResponse> result =
                appointmentService.getSpecialtyAppointmentStats(
                        monthStart,
                        nextMonthStart
                );

        return ResponseEntity.ok(result);
    }
}