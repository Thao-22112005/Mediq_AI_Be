package com.mediq.scheduling.repository;

import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<Slot, UUID> {

    List<Slot> findByWorkSchedule_ScheduleId(UUID scheduleId);

    List<Slot> findByWorkSchedule_ScheduleIdAndStatus(UUID scheduleId, SlotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.slotId = :slotId")
    Optional<Slot> findByIdWithLock(@Param("slotId") UUID slotId);

    @Query("""
        SELECT s FROM Slot s
        JOIN s.workSchedule ws
        WHERE (:doctorId IS NULL OR ws.doctorId = :doctorId)
          AND (:clinicId IS NULL OR ws.clinic.clinicId = :clinicId)
          AND (:specialtyId IS NULL OR ws.specialty.specialtyId = :specialtyId)
          AND (:date IS NULL OR ws.date = :date)
          AND (:status IS NULL OR s.status = :status)
        ORDER BY ws.date ASC, s.startTime ASC
    """)
    List<Slot> findSlotsWithFilter(
            @Param("doctorId") UUID doctorId,
            @Param("clinicId") UUID clinicId,
            @Param("specialtyId") UUID specialtyId,
            @Param("date") LocalDate date,
            @Param("status") SlotStatus status
    );
}
