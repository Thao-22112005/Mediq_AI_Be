package com.mediq.scheduling.repository;

import com.mediq.scheduling.entity.LeaveRequest;
import com.mediq.scheduling.entity.LeaveRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByDoctorIdOrderByStartDateTimeDesc(UUID doctorId);

    List<LeaveRequest> findByStatusOrderByStartDateTimeDesc(LeaveRequestStatus status);

    @Query("""
        SELECT COUNT(lr) > 0 FROM LeaveRequest lr
        WHERE lr.doctorId = :doctorId
          AND lr.status IN ('PENDING', 'APPROVED')
          AND lr.startDateTime < :endDateTime
          AND lr.endDateTime > :startDateTime
    """)
    boolean hasOverlappingLeave(
            @Param("doctorId") UUID doctorId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
