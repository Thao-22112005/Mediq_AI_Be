package com.mediqai.admin.controller;

import com.mediqai.admin.dto.request.AddDoctorSpecialtyRequest;
import com.mediqai.admin.dto.request.CreateDoctorRequest;
import com.mediqai.admin.dto.request.UpdateDoctorRequest;
import com.mediqai.admin.dto.response.AdminDoctorResponse;
import com.mediqai.admin.dto.response.DoctorResponse;
import com.mediqai.admin.dto.response.DoctorSpecialtyResponse;
import com.mediqai.admin.dto.response.SpecialtyResponse;
import com.mediqai.admin.service.AdminDoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<AdminDoctorResponse>> getAllDoctors(
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminDoctorService.getAllDoctors(
                        status,
                        token
                )
        );
    }

    // =========================================================
    // GET ALL SPECIALTIES
    // =========================================================

    @GetMapping("/specialties")
    public ResponseEntity<List<SpecialtyResponse>> getAllSpecialties(
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminDoctorService.getAllSpecialties(
                        status,
                        token
                )
        );
    }

    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<AdminDoctorResponse> getDoctorById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminDoctorService.getDoctorById(
                        id,
                        token
                )
        );
    }

    // =========================================================
    // CREATE DOCTOR
    // =========================================================

    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(
            @RequestBody CreateDoctorRequest request,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminDoctorService.createDoctor(
                                request,
                                token
                        )
                );
    }

    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    @PatchMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable UUID id,
            @RequestBody UpdateDoctorRequest request,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity.ok(
                adminDoctorService.updateDoctor(
                        id,
                        request,
                        token
                )
        );
    }

    // =========================================================
    // ADD SPECIALTY
    // =========================================================

    @PostMapping("/{id}/specialties")
    public ResponseEntity<DoctorSpecialtyResponse> addSpecialty(
            @PathVariable UUID id,
            @RequestBody AddDoctorSpecialtyRequest request,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminDoctorService.addSpecialty(
                                id,
                                request,
                                token
                        )
                );
    }

    // =========================================================
    // REMOVE SPECIALTY
    // =========================================================

    @DeleteMapping("/{id}/specialties/{specialtyId}")
    public ResponseEntity<Void> removeSpecialty(
            @PathVariable UUID id,
            @PathVariable UUID specialtyId,
            @RequestHeader("Authorization") String authorization
    ) {

        String token =
                authorization.replace("Bearer ", "");

        adminDoctorService.removeSpecialty(
                id,
                specialtyId,
                token
        );

        return ResponseEntity.noContent().build();
    }
}