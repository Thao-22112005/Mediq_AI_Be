package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.request.FindReplacementCandidatesRequest;
import com.mediq.scheduling.dto.response.ReplacementCandidateDto;
import com.mediq.scheduling.service.ReplacementDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/replacement-doctors")
@RequiredArgsConstructor
public class ReplacementDoctorController {

    private final ReplacementDoctorService replacementDoctorService;

    @GetMapping
    public ResponseEntity<List<ReplacementCandidateDto>> findReplacementCandidates(
            @RequestParam(value = "originalDoctorId", required = false) UUID originalDoctorId,
            @RequestParam(value = "specialtyId") UUID specialtyId,
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(value = "endTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(value = "currentClinicId", required = false) UUID currentClinicId
    ) {
        FindReplacementCandidatesRequest request = FindReplacementCandidatesRequest.builder()
                .originalDoctorId(originalDoctorId)
                .specialtyId(specialtyId)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .currentClinicId(currentClinicId)
                .build();

        List<ReplacementCandidateDto> candidates = replacementDoctorService.findReplacementCandidates(request);
        return ResponseEntity.ok(candidates);
    }
}
