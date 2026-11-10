package com.mediqai.appointment.repository;

import com.mediqai.appointment.entity.AppointmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentHistoryRepository
        extends JpaRepository<AppointmentHistory, UUID> {

    List<AppointmentHistory> findByAppointmentIdOrderByCreatedAtAsc(
            UUID appointmentId
    );
}