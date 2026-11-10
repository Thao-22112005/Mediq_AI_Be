package com.mediqai.admin.controller;

import com.mediqai.admin.dto.request.MedicalRecordRequest;
import com.mediqai.admin.dto.request.PatientRequest;
import com.mediqai.admin.dto.response.MedicalRecordResponse;
import com.mediqai.admin.dto.response.PatientResponse;
import com.mediqai.admin.service.AdminPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/patients")
@RequiredArgsConstructor
public class AdminPatientController {

    private final AdminPatientService adminPatientService;

    // =========================================================
    // GET ALL PATIENTS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.getAllPatients(token)
        );
    }

    // =========================================================
    // GET PATIENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.getPatientById(
                        id,
                        token
                )
        );
    }

    // =========================================================
    // UPDATE PATIENT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @RequestBody PatientRequest request,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.updatePatient(
                        id,
                        request,
                        token
                )
        );
    }

    // =========================================================
    // GET MEDICAL RECORDS
    // =========================================================

    @GetMapping("/{patientId}/medical-records")
    public ResponseEntity<List<MedicalRecordResponse>> getMedicalRecords(
            @PathVariable Long patientId,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.getMedicalRecords(
                        patientId,
                        token
                )
        );
    }

    // =========================================================
    // UPDATE MEDICAL RECORD
    // =========================================================

    @PutMapping("/medical-records/{recordId}")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable Long recordId,
            @RequestBody MedicalRecordRequest request,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminPatientService.updateMedicalRecord(
                        recordId,
                        request,
                        token
                )
        );
    }
}