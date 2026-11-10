package com.mediq.scheduling.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DoctorClientImpl implements DoctorClient {

    private final RestClient restClient;

    public DoctorClientImpl(
            RestClient.Builder restClientBuilder,
            @Value("${services.doctor-service.url:http://localhost:8087}") String doctorServiceUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(doctorServiceUrl).build();
    }

    private record DoctorSpecialtyDto(UUID specialtyId) {}

    @Override
    public boolean hasSpecialty(UUID doctorId, UUID specialtyId) {
        try {
            List<DoctorSpecialtyDto> specialties = restClient.get()
                    .uri("/doctors/{id}/specialties", doctorId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (specialties == null || specialties.isEmpty()) {
                return false;
            }

            return specialties.stream()
                    .anyMatch(s -> s.specialtyId() != null && s.specialtyId().equals(specialtyId));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<DoctorDto> getDoctor(UUID doctorId) {
        try {
            DoctorDto doctor = restClient.get()
                    .uri("/doctors/{id}", doctorId)
                    .retrieve()
                    .body(DoctorDto.class);
            return Optional.ofNullable(doctor);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isDoctorActive(UUID doctorId) {
        return getDoctor(doctorId)
                .map(d -> "ACTIVE".equalsIgnoreCase(d.status()))
                .orElse(false);
    }
}
