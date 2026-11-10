package com.mediqai.appointment.repository;

import com.mediqai.appointment.entity.ReplacementProposal;
import com.mediqai.appointment.entity.ReplacementProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReplacementProposalRepository
        extends JpaRepository<ReplacementProposal, UUID> {

    List<ReplacementProposal> findByAppointmentId(
            UUID appointmentId
    );

    List<ReplacementProposal> findByAppointmentIdAndStatus(
            UUID appointmentId,
            ReplacementProposalStatus status
    );

    List<ReplacementProposal> findByStatus(
            ReplacementProposalStatus status
    );
}