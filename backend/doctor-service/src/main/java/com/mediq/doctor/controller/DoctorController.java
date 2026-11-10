package com.mediq.doctor.controller;

import com.mediq.doctor.dto.request.AddDoctorSpecialtyRequest;
import com.mediq.doctor.dto.request.CreateDoctorRequest;
import com.mediq.doctor.dto.request.UpdateDoctorRequest;
import com.mediq.doctor.dto.response.DoctorResponse;
import com.mediq.doctor.dto.response.DoctorSpecialtyResponse;
import com.mediq.doctor.entity.DoctorStatus;
import com.mediq.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(@RequestBody CreateDoctorRequest request) {
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable("id") UUID doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> listDoctors(@RequestParam(value = "status", required = false) DoctorStatus status) {
        return ResponseEntity.ok(doctorService.getAllDoctors(status));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable("id") UUID doctorId,
            @RequestBody UpdateDoctorRequest request
    ) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorId, request));
    }

    @PostMapping("/{id}/specialties")
    public ResponseEntity<DoctorSpecialtyResponse> addSpecialty(
            @PathVariable("id") UUID doctorId,
            @RequestBody AddDoctorSpecialtyRequest request
    ) {
        DoctorSpecialtyResponse response = doctorService.addSpecialty(doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/specialties/{specialtyId}")
    public ResponseEntity<Void> removeSpecialty(
            @PathVariable("id") UUID doctorId,
            @PathVariable("specialtyId") UUID specialtyId
    ) {
        doctorService.removeSpecialty(doctorId, specialtyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/specialties")
    public ResponseEntity<List<DoctorSpecialtyResponse>> getDoctorSpecialties(@PathVariable("id") UUID doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorSpecialties(doctorId));
    }
}
