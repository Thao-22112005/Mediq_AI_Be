package com.mediq.scheduling.repository;

import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {

    List<WorkSchedule> findByDoctorId(UUID doctorId);

    List<WorkSchedule> findByClinic_ClinicId(UUID clinicId);

    List<WorkSchedule> findByDate(LocalDate date);

    List<WorkSchedule> findByStatus(WorkScheduleStatus status);

    @Query("""
        SELECT COUNT(ws) > 0 FROM WorkSchedule ws
        WHERE ws.doctorId = :doctorId
          AND ws.date = :date
          AND ws.status != com.mediq.scheduling.entity.WorkScheduleStatus.CANCELLED
          AND ws.startTime < :endTime
          AND ws.endTime > :startTime
          AND (:excludeScheduleId IS NULL OR ws.scheduleId != :excludeScheduleId)
    """)
    boolean hasDoctorOverlap(
            @Param("doctorId") UUID doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeScheduleId") UUID excludeScheduleId
    );

    @Query("""
        SELECT COUNT(ws) > 0 FROM WorkSchedule ws
        WHERE ws.room.roomId = :roomId
          AND ws.date = :date
          AND ws.status != com.mediq.scheduling.entity.WorkScheduleStatus.CANCELLED
          AND ws.startTime < :endTime
          AND ws.endTime > :startTime
          AND (:excludeScheduleId IS NULL OR ws.scheduleId != :excludeScheduleId)
    """)
    boolean hasRoomOverlap(
            @Param("roomId") UUID roomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeScheduleId") UUID excludeScheduleId
    );

    @Query("""
        SELECT ws FROM WorkSchedule ws
        WHERE ws.doctorId = :doctorId
          AND ws.status = com.mediq.scheduling.entity.WorkScheduleStatus.APPROVED
          AND ws.date >= :startDate
          AND ws.date <= :endDate
        ORDER BY ws.date ASC, ws.startTime ASC
    """)
    List<WorkSchedule> findApprovedSchedulesInDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
