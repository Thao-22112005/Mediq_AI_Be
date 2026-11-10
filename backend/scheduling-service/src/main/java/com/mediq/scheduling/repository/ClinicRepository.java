package com.mediq.scheduling.repository;

import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.ClinicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, UUID> {

    boolean existsByCode(String code);

    boolean existsByCodeAndClinicIdNot(String code, UUID clinicId);

    List<Clinic> findByStatus(ClinicStatus status);
}
