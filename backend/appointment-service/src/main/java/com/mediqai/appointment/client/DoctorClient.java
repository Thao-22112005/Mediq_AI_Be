package com.mediqai.appointment.client;

import com.mediqai.appointment.dto.response.DoctorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoctorClient {

    private final RestTemplate restTemplate;

    @Value("${services.doctor.url}")
    private String doctorServiceUrl;

    public DoctorResponse getDoctorById(UUID doctorId) {
        return restTemplate.getForObject(
                doctorServiceUrl + "/doctors/" + doctorId,
                DoctorResponse.class
        );
    }
}