package com.mediqai.admin.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public List<Map<String, Object>> getAllUsers(String token) {

        return restClient.get()
                .uri("/api/auth/internal/users")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> getUserById(
            String token,
            Long id
    ) {

        return restClient.get()
                .uri("/api/auth/internal/users/{id}", id)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> updateUserStatus(
            String token,
            Long id,
            String status
    ) {

        Map<String, String> body = Map.of(
                "status", status
        );

        return restClient.put()
                .uri("/api/auth/internal/users/{id}/status", id)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> updateUserRole(
            String token,
            Long id,
            String role
    ) {

        Map<String, String> body = Map.of(
                "role", role
        );

        return restClient.put()
                .uri("/api/auth/internal/users/{id}/role", id)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> deleteUser(
            String token,
            Long id
    ) {
        return restClient.delete()
                .uri("/api/auth/internal/users/{id}", id)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> createUser(
            String token,
            Map<String, String> body
    ) {
        return restClient.post()
                .uri("/api/auth/internal/users")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}