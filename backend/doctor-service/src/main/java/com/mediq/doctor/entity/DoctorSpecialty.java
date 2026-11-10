package com.mediq.doctor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctor_specialties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSpecialty {

    @Id
    @Column(name = "doctor_specialty_id")
    private UUID doctorSpecialtyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "specialty_id")
    private UUID specialtyId;

    @Column(name = "is_primary")
    private boolean isPrimary;

    @Column(name = "created_at")
    private Instant createdAt;

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}
