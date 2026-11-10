package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.request.CreateClinicRequest;
import com.mediq.scheduling.dto.request.UpdateClinicRequest;
import com.mediq.scheduling.dto.response.ClinicResponse;
import com.mediq.scheduling.entity.ClinicStatus;
import com.mediq.scheduling.service.ClinicService;
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
@RequestMapping("/clinics")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @PostMapping
    public ResponseEntity<ClinicResponse> createClinic(@RequestBody CreateClinicRequest request) {
        ClinicResponse response = clinicService.createClinic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicResponse> getClinicById(@PathVariable("id") UUID clinicId) {
        return ResponseEntity.ok(clinicService.getClinicById(clinicId));
    }

    @GetMapping
    public ResponseEntity<List<ClinicResponse>> listClinics(@RequestParam(value = "status", required = false) ClinicStatus status) {
        return ResponseEntity.ok(clinicService.getAllClinics(status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClinicResponse> updateClinic(
            @PathVariable("id") UUID clinicId,
            @RequestBody UpdateClinicRequest request
    ) {
        return ResponseEntity.ok(clinicService.updateClinic(clinicId, request));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ClinicResponse> activateClinic(@PathVariable("id") UUID clinicId) {
        return ResponseEntity.ok(clinicService.activateClinic(clinicId));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ClinicResponse> deactivateClinic(@PathVariable("id") UUID clinicId) {
        return ResponseEntity.ok(clinicService.deactivateClinic(clinicId));
    }
}
