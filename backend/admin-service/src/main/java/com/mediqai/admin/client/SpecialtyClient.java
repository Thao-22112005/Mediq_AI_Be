package com.mediqai.admin.client;

import com.mediqai.admin.dto.response.SpecialtyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class SpecialtyClient {

    private final RestClient restClient;

    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;

    public SpecialtyClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SpecialtyResponse> getAllSpecialties(
            String status,
            String token
    ) {

        String url = schedulingServiceUrl + "/specialties";

        if (status != null && !status.isBlank()) {
            url += "?status=" + status;
        }

        return restClient.get()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<SpecialtyResponse>
                                >() {}
                );
    }

    // =========================================================
    // GET SPECIALTY BY ID
    // =========================================================

    public SpecialtyResponse getSpecialtyById(
            UUID specialtyId,
            String token
    ) {

        return restClient.get()
                .uri(
                        schedulingServiceUrl
                                + "/specialties/{id}",
                        specialtyId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(SpecialtyResponse.class);
    }
}