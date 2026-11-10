package com.mediqai.patient.controller;

import com.mediqai.patient.dto.InternalPatientRequest;
import com.mediqai.patient.dto.PatientRequest;
import com.mediqai.patient.dto.PatientResponse;
import com.mediqai.patient.entity.Patient;
import com.mediqai.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // =========================================================
    // PATIENT - TỰ TẠO HỒ SƠ
    // =========================================================

    @PostMapping
    public ResponseEntity<Patient> createPatient(
            @RequestBody PatientRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                patientService.createPatient(
                        request,
                        email
                )
        );
    }

    // =========================================================
    // ADMIN - XEM DANH SÁCH BỆNH NHÂN
    // =========================================================

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {

        return ResponseEntity.ok(
                patientService.getAllPatients()
        );
    }

    // =========================================================
    // PATIENT - XEM HỒ SƠ CỦA MÌNH
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<Patient> getMyPatient(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                patientService.getMyPatient(email)
        );
    }

    // =========================================================
    // ADMIN - XEM CHI TIẾT BỆNH NHÂN
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                patientService.getPatientById(id)
        );
    }

    // =========================================================
    // PATIENT - TỰ CẬP NHẬT HỒ SƠ
    // =========================================================

    @PutMapping("/me")
    public ResponseEntity<Patient> updateMyPatient(
            @RequestBody PatientRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                patientService.updateMyPatient(
                        email,
                        request
                )
        );
    }

    // =========================================================
    // PATIENT - TỰ CẬP NHẬT AVATAR
    // =========================================================

    @PutMapping("/me/avatar")
    public ResponseEntity<Patient> updateMyAvatar(
            @RequestParam String avatarUrl,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                patientService.updateMyAvatar(
                        email,
                        avatarUrl
                )
        );
    }

    // =========================================================
    // ADMIN - CẬP NHẬT THÔNG TIN BỆNH NHÂN
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientRequest request
    ) {

        return ResponseEntity.ok(
                patientService.updatePatient(
                        id,
                        request
                )
        );
    }

    // =========================================================
    // ADMIN - CẬP NHẬT AVATAR BỆNH NHÂN
    // =========================================================

    @PutMapping("/{id}/avatar")
    public ResponseEntity<Patient> updateAvatar(
            @PathVariable Long id,
            @RequestParam String avatarUrl
    ) {

        return ResponseEntity.ok(
                patientService.updateAvatar(
                        id,
                        avatarUrl
                )
        );
    }

    // =========================================================
    // AUTH SERVICE -> PATIENT SERVICE
    // TẠO HỒ SƠ KHI ROLE = PATIENT
    // =========================================================

    @PostMapping("/internal")
    public ResponseEntity<Patient> createPatientInternal(
            @RequestBody InternalPatientRequest request
    ) {

        return ResponseEntity.ok(
                patientService.createPatientInternal(
                        request.getUserId(),
                        request.getFullName(),
                        request.getPhone()
                )
        );
    }

    // =========================================================
    // AUTH SERVICE -> PATIENT SERVICE
    // XÓA HỒ SƠ KHI ĐỔI ROLE
    // PATIENT -> ROLE KHÁC
    // =========================================================

    @DeleteMapping("/internal/user/{userId}")
    public ResponseEntity<Void> deletePatientInternal(
            @PathVariable Long userId
    ) {

        patientService.deletePatientByUserId(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<Patient> getPatientInternal(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                patientService.getPatientInternal(id)
        );
    }
}