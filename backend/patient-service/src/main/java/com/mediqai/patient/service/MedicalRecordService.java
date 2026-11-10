package com.mediqai.patient.service;

import com.mediqai.patient.dto.MedicalRecordRequest;
import com.mediqai.patient.dto.MedicalRecordResponse;
import com.mediqai.patient.entity.MedicalRecord;
import com.mediqai.patient.entity.Patient;
import com.mediqai.patient.repository.MedicalRecordRepository;
import com.mediqai.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    // =========================================================
    // CREATE MEDICAL RECORD
    // =========================================================

    public MedicalRecordResponse createMedicalRecord(
            MedicalRecordRequest request
    ) {

        Patient patient =
                patientRepository.findById(
                                request.getPatientId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy bệnh nhân ID: "
                                                + request.getPatientId()
                                )
                        );

        MedicalRecord record = new MedicalRecord();

        record.setSymptoms(request.getSymptoms());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setRecordDate(request.getRecordDate());
        record.setStatus(request.getStatus());
        record.setPatient(patient);

        MedicalRecord savedRecord =
                medicalRecordRepository.save(record);

        return mapToResponse(savedRecord);
    }

    // =========================================================
    // GET MEDICAL RECORDS BY PATIENT
    // =========================================================

    public List<MedicalRecordResponse> getRecordsByPatientId(
            Long patientId
    ) {

        List<MedicalRecord> records =
                medicalRecordRepository.findByPatientId(
                        patientId
                );

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // UPDATE MEDICAL RECORD
    // Admin sửa hồ sơ bệnh án
    // =========================================================

    public MedicalRecordResponse updateMedicalRecord(
            Long id,
            MedicalRecordRequest request
    ) {

        MedicalRecord record =
                medicalRecordRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy hồ sơ bệnh án ID: "
                                                + id
                                )
                        );

        // Không đổi patient của bệnh án
        // Chỉ cập nhật nội dung bệnh án

        record.setSymptoms(request.getSymptoms());
        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setRecordDate(request.getRecordDate());
        record.setStatus(request.getStatus());

        MedicalRecord updatedRecord =
                medicalRecordRepository.save(record);

        return mapToResponse(updatedRecord);
    }

    // =========================================================
    // MAP ENTITY -> RESPONSE
    // =========================================================

    private MedicalRecordResponse mapToResponse(
            MedicalRecord record
    ) {

        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(
                        record.getPatient().getId()
                )
                .symptoms(record.getSymptoms())
                .diagnosis(record.getDiagnosis())
                .treatment(record.getTreatment())
                .recordDate(record.getRecordDate())
                .status(record.getStatus())
                .build();
    }
}