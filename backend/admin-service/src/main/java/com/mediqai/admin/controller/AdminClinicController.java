 package com.mediqai.admin.controller;

import com.mediqai.admin.client.ClinicClient;
import com.mediqai.admin.dto.response.ClinicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/clinics")
@RequiredArgsConstructor
public class AdminClinicController {

    private final ClinicClient clinicClient;

    @GetMapping
    public List<ClinicResponse> getAllClinics(
            @RequestParam(required = false) String status,
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            ) String authorization
    ) {
        if (authorization == null || authorization.isBlank()) {
            throw new IllegalArgumentException("Missing Authorization token");
        }

        return clinicClient.getAllClinics(status, authorization);
    }
}

