package com.mediq.scheduling.repository;

import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.SpecialtyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    boolean existsByName(String name);

    boolean existsByNameAndSpecialtyIdNot(String name, UUID specialtyId);

    List<Specialty> findByStatus(SpecialtyStatus status);
}
