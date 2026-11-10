package com.mediqai.appointment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "replacement_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplacementProposal {

    @Id
    @Column(name = "proposal_id", nullable = false, updatable = false)
    @Builder.Default
    private UUID proposalId = UUID.randomUUID();

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "replacement_doctor_id", nullable = false)
    private UUID replacementDoctorId;

    @Column(name = "clinic_id", nullable = false)
    private UUID clinicId;

    @Column(name = "specialty_id", nullable = false)
    private UUID specialtyId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "slot_id", nullable = false)
    private UUID slotId;

    @Column(name = "proposed_date", nullable = false)
    private LocalDate proposedDate;

    @Column(name = "proposed_start_time", nullable = false)
    private LocalTime proposedStartTime;

    @Column(name = "proposed_end_time", nullable = false)
    private LocalTime proposedEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReplacementProposalStatus status = ReplacementProposalStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {

        if (proposalId == null) {
            proposalId = UUID.randomUUID();
        }

        if (status == null) {
            status = ReplacementProposalStatus.PENDING;
        }

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}