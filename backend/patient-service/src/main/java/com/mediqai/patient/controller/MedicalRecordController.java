package com.mediqai.patient.controller;

import com.mediqai.patient.dto.MedicalRecordRequest;
import com.mediqai.patient.dto.MedicalRecordResponse;
import com.mediqai.patient.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // =========================================================
    // TẠO HỒ SƠ BỆNH ÁN
    // =========================================================

    @PostMapping
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @RequestBody MedicalRecordRequest request
    ) {

        return ResponseEntity.ok(
                medicalRecordService.createMedicalRecord(request)
        );
    }

    // =========================================================
    // XEM TẤT CẢ BỆNH ÁN CỦA BỆNH NHÂN
    // =========================================================

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>> getRecordsByPatientId(
            @PathVariable Long patientId
    ) {

        return ResponseEntity.ok(
                medicalRecordService.getRecordsByPatientId(patientId)
        );
    }

    // =========================================================
    // ADMIN - SỬA HỒ SƠ BỆNH ÁN
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable Long id,
            @RequestBody MedicalRecordRequest request
    ) {

        return ResponseEntity.ok(
                medicalRecordService.updateMedicalRecord(
                        id,
                        request
                )
        );
    }
}