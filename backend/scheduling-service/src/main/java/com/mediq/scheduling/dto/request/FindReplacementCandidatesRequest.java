package com.mediq.scheduling.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindReplacementCandidatesRequest {

    private UUID originalDoctorId;
    private UUID specialtyId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID currentClinicId;
}
