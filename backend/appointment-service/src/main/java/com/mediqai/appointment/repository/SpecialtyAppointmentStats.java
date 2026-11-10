package com.mediqai.appointment.repository;

import java.util.UUID;

public interface SpecialtyAppointmentStats {

    UUID getSpecialtyId();

    Long getPatientsThisMonth();
}