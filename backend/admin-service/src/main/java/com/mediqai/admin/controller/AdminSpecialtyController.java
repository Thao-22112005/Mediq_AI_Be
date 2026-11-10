        package com.mediqai.admin.controller;

import com.mediqai.admin.client.SpecialtyClient;
import com.mediqai.admin.dto.response.SpecialtyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/specialties")
@RequiredArgsConstructor
public class AdminSpecialtyController {

    private final SpecialtyClient specialtyClient;

    @GetMapping
    public List<SpecialtyResponse> getAllSpecialties(
            @RequestParam(required = false) String status,
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            ) String authorization
    ) {
        String token = extractToken(authorization);
        return specialtyClient.getAllSpecialties(status, token);
    }

    private String extractToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Missing Authorization token"
            );
        }

        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        return authorization;
    }
}

