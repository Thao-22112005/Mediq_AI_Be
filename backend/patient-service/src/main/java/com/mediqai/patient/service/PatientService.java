package com.mediqai.patient.service;

import com.mediqai.patient.client.AuthClient;
import com.mediqai.patient.dto.PatientRequest;
import com.mediqai.patient.dto.PatientResponse;
import com.mediqai.patient.dto.UserResponse;
import com.mediqai.patient.entity.MedicalRecord;
import com.mediqai.patient.entity.Patient;
import com.mediqai.patient.repository.MedicalRecordRepository;
import com.mediqai.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AuthClient authClient;

    // =========================================================
    // CREATE PATIENT
    // Patient tự tạo hồ sơ
    // =========================================================

    public Patient createPatient(
            PatientRequest request,
            String email
    ) {

        UserResponse user = authClient.getUserByEmail(email);

        if (patientRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException(
                    "Tài khoản này đã có hồ sơ bệnh nhân"
            );
        }

        Patient patient = new Patient();

        patient.setUserId(user.getId());
        patient.setPatientCode(generatePatientCode());
        patient.setFullName(request.getFullName());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setBloodType(request.getBloodType());
        patient.setMedicalHistory(request.getMedicalHistory());

        // Avatar ban đầu có thể null
        patient.setAvatarUrl(request.getAvatarUrl());

        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(
                    request.getDateOfBirth().toString()
            );
        }

        return patientRepository.save(patient);
    }

    // =========================================================
    // GET ALL PATIENTS
    // Admin xem danh sách
    // =========================================================

    public List<PatientResponse> getAllPatients() {

        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET PATIENT BY ID
    // Admin xem chi tiết
    // =========================================================

    public PatientResponse getPatientById(Long id) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy bệnh nhân ID: " + id
                        )
                );

        return mapToResponse(patient);
    }

    // =========================================================
    // UPDATE MY PATIENT
    // Patient tự sửa hồ sơ
    // =========================================================

    public Patient updateMyPatient(
            String email,
            PatientRequest request
    ) {

        Patient patient = getMyPatientEntity(email);

        updatePatientFields(patient, request);

        return patientRepository.save(patient);
    }

    // =========================================================
    // GET MY PATIENT
    // =========================================================

    public Patient getMyPatient(String email) {

        return getMyPatientEntity(email);
    }

    private Patient getMyPatientEntity(String email) {

        UserResponse user = authClient.getUserByEmail(email);

        return patientRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hồ sơ bệnh nhân"
                        )
                );
    }

    // =========================================================
    // UPDATE PATIENT BY ADMIN
    // Admin sửa thông tin bệnh nhân
    // =========================================================

    public PatientResponse updatePatient(
            Long id,
            PatientRequest request
    ) {

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy bệnh nhân ID: " + id
                        )
                );

        updatePatientFields(patient, request);

        Patient updatedPatient =
                patientRepository.save(patient);

        return mapToResponse(updatedPatient);
    }

    // =========================================================
    // UPDATE AVATAR
    // Media Service / Frontend gọi sau khi upload Cloudinary
    // =========================================================

    public Patient updateAvatar(
            Long id,
            String avatarUrl
    ) {

        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Avatar URL không được để trống"
            );
        }

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy bệnh nhân ID: " + id
                        )
                );

        patient.setAvatarUrl(avatarUrl);

        return patientRepository.save(patient);
    }

    // =========================================================
    // UPDATE MY AVATAR
    // Patient tự cập nhật avatar
    // =========================================================

    public Patient updateMyAvatar(
            String email,
            String avatarUrl
    ) {

        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Avatar URL không được để trống"
            );
        }

        Patient patient = getMyPatientEntity(email);

        patient.setAvatarUrl(avatarUrl);

        return patientRepository.save(patient);
    }

    // =========================================================
    // UPDATE PATIENT FIELDS
    // =========================================================

    private void updatePatientFields(
            Patient patient,
            PatientRequest request
    ) {

        patient.setFullName(request.getFullName());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setBloodType(request.getBloodType());
        patient.setMedicalHistory(request.getMedicalHistory());

        /*
         * Avatar được xử lý bằng API riêng.
         * Không update avatarUrl ở đây để tránh
         * việc PUT profile vô tình ghi đè avatar.
         */

        if (request.getDateOfBirth() != null) {

            patient.setDateOfBirth(
                    request.getDateOfBirth().toString()
            );
        }
    }

    // =========================================================
    // INTERNAL CREATE
    // Auth Service gọi khi role = PATIENT
    // =========================================================

    public Patient createPatientInternal(
            Long userId,
            String fullName,
            String phone
    ) {

        if (patientRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException(
                    "Tài khoản này đã có hồ sơ bệnh nhân"
            );
        }

        Patient patient = new Patient();

        patient.setUserId(userId);
        patient.setPatientCode(generatePatientCode());
        patient.setFullName(fullName);
        patient.setPhone(phone);

        // Avatar mặc định là null
        patient.setAvatarUrl(null);

        return patientRepository.save(patient);
    }

    // =========================================================
    // INTERNAL DELETE
    // Auth Service gọi khi đổi PATIENT -> ROLE KHÁC
    // =========================================================

    public void deletePatientByUserId(Long userId) {

        patientRepository.findByUserId(userId)
                .ifPresent(patientRepository::delete);
    }

    // =========================================================
    // GENERATE PATIENT CODE
    // =========================================================

    private String generatePatientCode() {

        String code;

        do {

            int number =
                    10000 + new Random().nextInt(90000);

            code = "PAT-" + number;

        } while (
                patientRepository
                        .findByPatientCode(code)
                        .isPresent()
        );

        return code;
    }

    // =========================================================
    // CALCULATE AGE
    // =========================================================

    private Integer calculateAge(String dateOfBirth) {

        if (dateOfBirth == null || dateOfBirth.isBlank()) {
            return null;
        }

        try {

            LocalDate dob =
                    LocalDate.parse(dateOfBirth);

            return Period.between(
                    dob,
                    LocalDate.now()
            ).getYears();

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // MAP PATIENT -> RESPONSE
    // =========================================================

    private PatientResponse mapToResponse(
            Patient patient
    ) {

        String email = null;

        try {

            UserResponse user =
                    authClient.getUserById(
                            patient.getUserId()
                    );

            if (user != null) {
                email = user.getEmail();
            }

        } catch (Exception ignored) {
            // Auth Service lỗi thì vẫn trả Patient
        }

        // =====================================================
        // LẤY MEDICAL RECORD MỚI NHẤT
        // =====================================================

        MedicalRecord latestRecord =
                medicalRecordRepository
                        .findFirstByPatientIdOrderByRecordDateDescIdDesc(
                                patient.getId()
                        )
                        .orElse(null);

        // =====================================================
        // PARSE DATE OF BIRTH
        // =====================================================

        LocalDate dateOfBirth = null;

        if (patient.getDateOfBirth() != null
                && !patient.getDateOfBirth().isBlank()) {

            try {

                dateOfBirth =
                        LocalDate.parse(
                                patient.getDateOfBirth()
                        );

            } catch (Exception ignored) {
            }
        }

        // =====================================================
        // BUILD RESPONSE
        // =====================================================

        return PatientResponse.builder()
                .id(patient.getId())
                .userId(patient.getUserId())
                .patientCode(patient.getPatientCode())
                .fullName(patient.getFullName())
                .email(email)
                .phone(patient.getPhone())
                .gender(patient.getGender())

                // Nếu chưa có ngày sinh thì trả null
                .dateOfBirth(
                        dateOfBirth != null
                                ? dateOfBirth.toString()
                                : null
                )

                .age(calculateAge(patient.getDateOfBirth()))
                .address(patient.getAddress())
                .bloodType(patient.getBloodType())
                .medicalHistory(patient.getMedicalHistory())

                // Avatar từ Patient
                .avatarUrl(patient.getAvatarUrl())

                // Lấy từ MedicalRecord mới nhất
                .symptoms(
                        latestRecord != null
                                ? latestRecord.getSymptoms()
                                : null
                )

                .status(
                        latestRecord != null
                                ? latestRecord.getStatus()
                                : null
                )

                .build();
    }

    public Patient getPatientInternal(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy bệnh nhân ID: " + id
                        )
                );
    }
}