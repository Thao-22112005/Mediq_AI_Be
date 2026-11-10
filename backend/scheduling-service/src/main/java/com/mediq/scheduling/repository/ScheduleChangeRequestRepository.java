package com.mediq.scheduling.repository;

import com.mediq.scheduling.entity.ScheduleChangeRequest;
import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleChangeRequestRepository extends JpaRepository<ScheduleChangeRequest, UUID> {

    List<ScheduleChangeRequest> findBySchedule_ScheduleId(UUID scheduleId);

    List<ScheduleChangeRequest> findByDoctorId(UUID doctorId);

    List<ScheduleChangeRequest> findByStatus(ScheduleChangeRequestStatus status);

    List<ScheduleChangeRequest> findByDoctorIdAndStatus(UUID doctorId, ScheduleChangeRequestStatus status);

    Optional<ScheduleChangeRequest> findFirstBySchedule_ScheduleIdAndStatus(UUID scheduleId, ScheduleChangeRequestStatus status);

    boolean existsBySchedule_ScheduleIdAndStatus(UUID scheduleId, ScheduleChangeRequestStatus status);
}
