        package com.mediqai.admin.client;

import com.mediqai.admin.dto.request.CreateRoomRequest;
import com.mediqai.admin.dto.request.UpdateRoomRequest;
import com.mediqai.admin.dto.response.RoomResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class RoomClient {

    private final RestClient restClient;

    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;

    public RoomClient(RestClient restClient) {
        this.restClient = restClient;
    }


    public List<RoomResponse> getRoomsByClinic(
            UUID clinicId,
            String status,
            String token
    ) {
        String baseUrl = schedulingServiceUrl.replaceAll("/+$", "");
        String url = baseUrl + "/clinics/{clinicId}/rooms";

        if (status != null && !status.isBlank()) {
            url += "?status={status}";

            return restClient.get()
                    .uri(url, clinicId, status)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<
                            List<RoomResponse>>() {});
        }

        return restClient.get()
                .uri(url, clinicId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<
                        List<RoomResponse>>() {});
    }



    public RoomResponse getRoomById(UUID roomId, String token) {
        return restClient.get()
                .uri(schedulingServiceUrl + "/rooms/{id}", roomId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(RoomResponse.class);
    }

    public RoomResponse createRoom(
            UUID clinicId,
            CreateRoomRequest request,
            String token
    ) {
        return restClient.post()
                .uri(schedulingServiceUrl + "/clinics/{clinicId}/rooms", clinicId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(request)
                .retrieve()
                .body(RoomResponse.class);
    }

    public RoomResponse updateRoom(
            UUID roomId,
            UpdateRoomRequest request,
            String token
    ) {
        return restClient.patch()
                .uri(schedulingServiceUrl + "/rooms/{id}", roomId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(request)
                .retrieve()
                .body(RoomResponse.class);
    }
}

