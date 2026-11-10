package com.mediqai.admin.client;

import com.mediqai.admin.dto.request.CreateWorkScheduleRequest;
import com.mediqai.admin.dto.response.WorkScheduleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class WorkScheduleClient {

    private final RestClient restClient;

    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;

    public WorkScheduleClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<WorkScheduleResponse> getSchedulesByDoctor(
            UUID doctorId,
            String token
    ) {

        return restClient.get()
                .uri(
                        schedulingServiceUrl
                                + "/work-schedules?doctorId="
                                + doctorId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<WorkScheduleResponse>
                                >() {}
                );
    }

    // =========================================================
    // GET SCHEDULE BY ID
    // =========================================================

    public WorkScheduleResponse getScheduleById(
            UUID scheduleId,
            String token
    ) {

        return restClient.get()
                .uri(
                        schedulingServiceUrl
                                + "/work-schedules/{id}",
                        scheduleId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(WorkScheduleResponse.class);
    }

    public WorkScheduleResponse createWorkSchedule(
            CreateWorkScheduleRequest request,
            String token
    ) {
        return restClient.post()
                .uri(schedulingServiceUrl + "/work-schedules")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .body(request)
                .retrieve()
                .body(WorkScheduleResponse.class);
    }




    public List<WorkScheduleResponse> getSchedulesByRoomAndDate(
            UUID roomId,
            java.time.LocalDate date,
            String token
    ) {
        return restClient.get()
                .uri(
                        schedulingServiceUrl
                                + "/work-schedules?roomId={roomId}&date={date}",
                        roomId,
                        date
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<WorkScheduleResponse>
                                >() {}
                );
    }



}