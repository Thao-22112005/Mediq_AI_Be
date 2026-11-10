package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.request.CreateSpecialtyRequest;
import com.mediq.scheduling.dto.request.UpdateSpecialtyRequest;
import com.mediq.scheduling.dto.response.SpecialtyResponse;
import com.mediq.scheduling.entity.SpecialtyStatus;
import com.mediq.scheduling.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/specialties")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @PostMapping
    public ResponseEntity<SpecialtyResponse> createSpecialty(@RequestBody CreateSpecialtyRequest request) {
        SpecialtyResponse response = specialtyService.createSpecialty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialtyResponse> getSpecialtyById(@PathVariable("id") UUID specialtyId) {
        return ResponseEntity.ok(specialtyService.getSpecialtyById(specialtyId));
    }

    @GetMapping
    public ResponseEntity<List<SpecialtyResponse>> listSpecialties(@RequestParam(value = "status", required = false) SpecialtyStatus status) {
        return ResponseEntity.ok(specialtyService.getAllSpecialties(status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SpecialtyResponse> updateSpecialty(
            @PathVariable("id") UUID specialtyId,
            @RequestBody UpdateSpecialtyRequest request
    ) {
        return ResponseEntity.ok(specialtyService.updateSpecialty(specialtyId, request));
    }
}
