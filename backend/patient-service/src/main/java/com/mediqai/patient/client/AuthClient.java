package com.mediqai.patient.client;

import com.mediqai.patient.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AuthClient {

    private final RestClient restClient;

    public UserResponse getUserByEmail(String email) {

        return restClient.get()
                .uri(
                        "http://localhost:8081/api/auth/internal/users/by-email/{email}",
                        email
                )
                .retrieve()
                .body(UserResponse.class);
    }

    public UserResponse getUserById(Long userId) {

        return restClient.get()
                .uri(
                        "http://localhost:8081/api/auth/internal/users/{id}",
                        userId
                )
                .retrieve()
                .body(UserResponse.class);
    }
}