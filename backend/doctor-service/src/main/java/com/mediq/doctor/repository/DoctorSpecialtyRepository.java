package com.mediq.doctor.repository;

import com.mediq.doctor.entity.DoctorSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorSpecialtyRepository extends JpaRepository<DoctorSpecialty, UUID> {

    List<DoctorSpecialty> findByDoctor_DoctorId(UUID doctorId);

    Optional<DoctorSpecialty> findByDoctor_DoctorIdAndIsPrimaryTrue(UUID doctorId);

    boolean existsByDoctor_DoctorIdAndSpecialtyId(UUID doctorId, UUID specialtyId);

    void deleteByDoctor_DoctorIdAndSpecialtyId(UUID doctorId, UUID specialtyId);


    @Modifying
    @Query("DELETE FROM DoctorSpecialty ds WHERE ds.doctor.doctorId = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") UUID doctorId);

}
