package com.mediqai.admin.service;

import com.mediqai.admin.client.DoctorClient;
import com.mediqai.admin.client.RoomClient;
import com.mediqai.admin.client.SpecialtyClient;
import com.mediqai.admin.client.WorkScheduleClient;
import com.mediqai.admin.dto.request.AddDoctorSpecialtyRequest;
import com.mediqai.admin.dto.request.CreateDoctorRequest;
import com.mediqai.admin.dto.request.UpdateDoctorRequest;
import com.mediqai.admin.dto.response.AdminDoctorResponse;
import com.mediqai.admin.dto.response.AdminDoctorScheduleResponse;
import com.mediqai.admin.dto.response.AdminDoctorSpecialtyResponse;
import com.mediqai.admin.dto.response.DoctorResponse;
import com.mediqai.admin.dto.response.DoctorSpecialtyResponse;
import com.mediqai.admin.dto.response.RoomResponse;
import com.mediqai.admin.dto.response.SpecialtyResponse;
import com.mediqai.admin.dto.response.WorkScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminDoctorService {

    private final DoctorClient doctorClient;
    private final SpecialtyClient specialtyClient;
    private final WorkScheduleClient workScheduleClient;
    private final RoomClient roomClient;

    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    public List<AdminDoctorResponse> getAllDoctors(
            String status,
            String token
    ) {

        List<DoctorResponse> doctors =
                doctorClient.getAllDoctors(status, token);

        return doctors.stream()
                .map(doctor ->
                        buildAdminDoctorResponse(
                                doctor,
                                token
                        )
                )
                .toList();
    }

    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    public AdminDoctorResponse getDoctorById(
            UUID doctorId,
            String token
    ) {

        DoctorResponse doctor =
                doctorClient.getDoctorById(
                        doctorId,
                        token
                );

        return buildAdminDoctorResponse(
                doctor,
                token
        );
    }

    // =========================================================
    // CREATE DOCTOR
    // =========================================================

    public DoctorResponse createDoctor(
            CreateDoctorRequest request,
            String token
    ) {

        return doctorClient.createDoctor(
                request,
                token
        );
    }

    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    public DoctorResponse updateDoctor(
            UUID doctorId,
            UpdateDoctorRequest request,
            String token
    ) {

        return doctorClient.updateDoctor(
                doctorId,
                request,
                token
        );
    }

    // =========================================================
    // ADD SPECIALTY TO DOCTOR
    // =========================================================

    public DoctorSpecialtyResponse addSpecialty(
            UUID doctorId,
            AddDoctorSpecialtyRequest request,
            String token
    ) {

        return doctorClient.addSpecialty(
                doctorId,
                request,
                token
        );
    }

    // =========================================================
    // REMOVE SPECIALTY FROM DOCTOR
    // =========================================================

    public void removeSpecialty(
            UUID doctorId,
            UUID specialtyId,
            String token
    ) {

        doctorClient.removeSpecialty(
                doctorId,
                specialtyId,
                token
        );
    }

    // =========================================================
    // GET ALL SPECIALTIES
    // =========================================================

    public List<SpecialtyResponse> getAllSpecialties(
            String status,
            String token
    ) {

        return specialtyClient.getAllSpecialties(
                status,
                token
        );
    }

    // =========================================================
    // BUILD ADMIN DOCTOR RESPONSE
    // =========================================================

    private AdminDoctorResponse buildAdminDoctorResponse(
            DoctorResponse doctor,
            String token
    ) {

        // ---------------------------------------------------------
        // SPECIALTIES
        // ---------------------------------------------------------

        List<DoctorSpecialtyResponse> doctorSpecialties =
                doctor.getSpecialties() != null
                        ? doctor.getSpecialties()
                        : Collections.emptyList();

        List<AdminDoctorSpecialtyResponse> specialties =
                doctorSpecialties.stream()
                        .map(specialty -> {

                            SpecialtyResponse specialtyResponse =
                                    specialtyClient.getSpecialtyById(
                                            specialty.getSpecialtyId(),
                                            token
                                    );

                            return AdminDoctorSpecialtyResponse.builder()
                                    .specialtyId(
                                            specialty.getSpecialtyId()
                                    )
                                    .name(
                                            specialtyResponse.getName()
                                    )
                                    .description(
                                            specialtyResponse.getDescription()
                                    )
                                    .primary(
                                            specialty.isPrimary()
                                    )
                                    .build();
                        })
                        .toList();

        // ---------------------------------------------------------
        // WORK SCHEDULES
        // ---------------------------------------------------------

        List<WorkScheduleResponse> schedules =
                workScheduleClient.getSchedulesByDoctor(
                        doctor.getDoctorId(),
                        token
                );

        List<AdminDoctorScheduleResponse> adminSchedules =
                schedules.stream()
                        .map(schedule -> {

                            String roomCode = null;
                            String roomName = null;

                            if (schedule.getRoomId() != null) {

                                RoomResponse room =
                                        roomClient.getRoomById(
                                                schedule.getRoomId(),
                                                token
                                        );

                                if (room != null) {
                                    roomCode = room.getCode();
                                    roomName = room.getName();
                                }
                            }

                            return AdminDoctorScheduleResponse.builder()
                                    .scheduleId(
                                            schedule.getScheduleId()
                                    )
                                    .clinicId(
                                            schedule.getClinicId()
                                    )
                                    .specialtyId(
                                            schedule.getSpecialtyId()
                                    )
                                    .roomId(
                                            schedule.getRoomId()
                                    )
                                    .roomCode(roomCode)
                                    .roomName(roomName)
                                    .date(
                                            schedule.getDate()
                                    )
                                    .startTime(
                                            schedule.getStartTime()
                                    )
                                    .endTime(
                                            schedule.getEndTime()
                                    )
                                    .status(
                                            schedule.getStatus()
                                    )
                                    .build();
                        })
                        .toList();

        // ---------------------------------------------------------
        // FINAL RESPONSE
        // ---------------------------------------------------------

        return AdminDoctorResponse.builder()
                .doctorId(doctor.getDoctorId())
                .userId(doctor.getUserId())
                .fullName(doctor.getFullName())
                .phoneNumber(doctor.getPhoneNumber())
                .licenseNumber(doctor.getLicenseNumber())
                .status(doctor.getStatus())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .specialties(specialties)
                .workSchedules(adminSchedules)
                .build();
    }
}