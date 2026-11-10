package com.mediqai.patient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "patient_code", unique = true)
    private String patientCode;

    private String fullName;

    private String gender;

    private String dateOfBirth;

    private String phone;

    private String address;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @OneToMany(
            mappedBy = "patient",
            cascade = CascadeType.ALL
    )
    private List<MedicalRecord> medicalRecords;
}