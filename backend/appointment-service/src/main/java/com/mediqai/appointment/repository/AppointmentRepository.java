package com.mediqai.appointment.repository;

import com.mediqai.appointment.entity.Appointment;
import com.mediqai.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(UUID doctorId);

    List<Appointment> findByClinicId(UUID clinicId);

    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByPatientIdAndStatus(
            UUID patientId,
            AppointmentStatus status
    );

    List<Appointment> findByDoctorIdAndAppointmentDate(
            UUID doctorId,
            LocalDate appointmentDate
    );

    @Query("""
    SELECT a.specialtyId AS specialtyId,
           COUNT(a.appointmentId) AS patientsThisMonth
    FROM Appointment a
    WHERE a.appointmentDate >= :monthStart
      AND a.appointmentDate < :nextMonthStart
      AND a.status = :status
    GROUP BY a.specialtyId
    """)
    List<SpecialtyAppointmentStats> countCompletedAppointmentsBySpecialty(
            @Param("monthStart") LocalDate monthStart,
            @Param("nextMonthStart") LocalDate nextMonthStart,
            @Param("status") AppointmentStatus status
    );
}