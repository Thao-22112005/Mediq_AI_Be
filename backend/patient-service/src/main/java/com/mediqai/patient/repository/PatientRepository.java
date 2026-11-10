package com.mediqai.patient.repository;

import com.mediqai.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository
        extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByPatientCode(String patientCode);
}