
package com.mediqai.admin.service;

import com.mediqai.admin.client.*;

import com.mediqai.admin.dto.response.*;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AdminAppointmentService {

    private final AppointmentClient appointmentClient;
    private final DoctorClient doctorClient;
    private final SpecialtyClient specialtyClient;
    private final RoomClient roomClient;
    private final PatientClient patientClient;
    private final ClinicClient clinicClient;

    public AdminAppointmentService(
            AppointmentClient appointmentClient,
            DoctorClient doctorClient,
            SpecialtyClient specialtyClient,
            RoomClient roomClient,
            PatientClient patientClient,
            ClinicClient clinicClient
    ) {
        this.appointmentClient = appointmentClient;
        this.doctorClient = doctorClient;
        this.specialtyClient = specialtyClient;
        this.roomClient = roomClient;
        this.patientClient = patientClient;
        this.clinicClient = clinicClient;
    }

    public List<AdminAppointmentResponse> getAppointments(
            LocalDate date,
            String status,
            UUID doctorId,
            Long patientId,
            String token
    ) {
        List<AdminAppointmentResponse> appointments =
                appointmentClient.getAppointments(
                        date, status, doctorId, patientId, token
                );

        if (appointments == null || appointments.isEmpty()) {
            return List.of();
        }

        return appointments.stream()
                .map(a -> enrichAppointment(a, token))
                .toList();
    }

    public AdminAppointmentResponse getAppointmentById(
            UUID appointmentId,
            String token
    ) {
        AdminAppointmentResponse appointment =
                appointmentClient.getAppointmentById(
                        appointmentId, token
                );

        return enrichAppointment(appointment, token);
    }

    private AdminAppointmentResponse enrichAppointment(
            AdminAppointmentResponse a,
            String token
    ) {
        String doctorNames = lookupDoctorName(a.doctorId(), token);
        String specialtyNames = lookupSpecialtyName(a.specialtyId(), token);
        String roomNames = lookupRoomName(a.roomId(), token);
        String patientNames = lookupPatientName(a.patientId(), token);
        String clinicNames = lookupClinicName(
                a.clinicId(),
                token
        );

        return new AdminAppointmentResponse(
                a.appointmentId(),
                a.patientId(),
                a.doctorId(),
                a.clinicId(),
                a.specialtyId(),
                a.roomId(),
                a.slotId(),
                a.appointmentDate(),
                a.startTime(),
                a.endTime(),
                a.status(),
                a.reason(),
                a.createdAt(),
                a.updatedAt(),
                patientNames,
                clinicNames,
                doctorNames,
                specialtyNames,
                roomNames
        );
    }

    private String lookupDoctorName(UUID doctorId, String token) {
        if (doctorId == null) return null;

        try {
            DoctorResponse doctor =
                    doctorClient.getDoctorById(doctorId, token);

            if (doctor == null) return null;

            // TODO: thay getFullName() bằng getter thực tế
            // trong DoctorResponse của dự án.
            return doctor.getFullName();

        } catch (RestClientException ex) {
            return null;
        }
    }

    private String lookupSpecialtyName(UUID specialtyId, String token) {
        if (specialtyId == null) return null;

        try {
            SpecialtyResponse specialty =
                    specialtyClient.getSpecialtyById(specialtyId, token);

            if (specialty == null) return null;

            // TODO: thay getName() bằng getter thực tế
            // trong SpecialtyResponse của dự án.
            return specialty.getName();

        } catch (RestClientException ex) {
            return null;
        }
    }

    private String lookupRoomName(UUID roomId, String token) {
        if (roomId == null) return null;

        try {
            RoomResponse room =
                    roomClient.getRoomById(roomId, token);

            if (room == null) return null;

            // TODO: thay getName() bằng getter thực tế
            // trong RoomResponse của dự án.
            return room.getName();

        } catch (RestClientException ex) {
            return null;
        }
    }

    private String lookupPatientName(Long patientId, String token) {
        if (patientId == null) {
            return null;
        }

        try {
            PatientResponse patient =
                    patientClient.getPatientById(patientId, token);

            if (patient == null) {
                return null;
            }

            // Thay getFullName() bằng getter tên thật trong PatientResponse
            return patient.getFullName();

        } catch (org.springframework.web.client.RestClientException ex) {
            return null;
        }
    }

    private String lookupClinicName(UUID clinicId, String token) {
        if (clinicId == null) {
            return null;
        }

        try {
            ClinicResponse clinic =
                    clinicClient.getClinicById(clinicId, token);

            return clinic != null ? clinic.name() : null;
        } catch (RestClientException ex) {
            return null;
        }
    }
}
