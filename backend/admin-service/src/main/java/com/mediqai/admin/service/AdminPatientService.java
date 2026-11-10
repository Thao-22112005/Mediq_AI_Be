package com.mediqai.admin.service;

import com.mediqai.admin.client.PatientClient;
import com.mediqai.admin.dto.request.MedicalRecordRequest;
import com.mediqai.admin.dto.request.PatientRequest;
import com.mediqai.admin.dto.response.MedicalRecordResponse;
import com.mediqai.admin.dto.response.PatientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPatientService {

    private final PatientClient patientClient;

    // =========================================================
    // GET ALL PATIENTS
    // =========================================================

    public List<PatientResponse> getAllPatients(
            String token
    ) {

        return patientClient.getAllPatients(token);
    }

    // =========================================================
    // GET PATIENT BY ID
    // =========================================================

    public PatientResponse getPatientById(
            Long id,
            String token
    ) {

        return patientClient.getPatientById(
                id,
                token
        );
    }

    // =========================================================
    // UPDATE PATIENT
    // =========================================================

    public PatientResponse updatePatient(
            Long id,
            PatientRequest request,
            String token
    ) {

        return patientClient.updatePatient(
                id,
                request,
                token
        );
    }

    // =========================================================
    // GET MEDICAL RECORDS
    // =========================================================

    public List<MedicalRecordResponse> getMedicalRecords(
            Long patientId,
            String token
    ) {

        return patientClient.getMedicalRecords(
                patientId,
                token
        );
    }

    // =========================================================
    // UPDATE MEDICAL RECORD
    // =========================================================

    public MedicalRecordResponse updateMedicalRecord(
            Long id,
            MedicalRecordRequest request,
            String token
    ) {

        return patientClient.updateMedicalRecord(
                id,
                request,
                token
        );
    }
}