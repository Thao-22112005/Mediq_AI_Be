package com.mediqai.appointment.client;

import com.mediqai.appointment.dto.response.ClinicResponse;
import com.mediqai.appointment.dto.response.RoomResponse;
import com.mediqai.appointment.dto.response.SlotResponse;
import com.mediqai.appointment.dto.response.SpecialtyResponse;
import com.mediqai.appointment.dto.response.WorkScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SchedulingClient {

    private final RestClient restClient;

    @Value("${services.scheduling.url}")
    private String schedulingServiceUrl;

    public SlotResponse getSlotById(UUID slotId) {
        return restClient.get()
                .uri(schedulingServiceUrl + "/slots/{id}", slotId)
                .retrieve()
                .body(SlotResponse.class);
    }

    public SlotResponse holdSlot(UUID slotId) {
        return restClient.post()
                .uri(schedulingServiceUrl + "/slots/{id}/hold", slotId)
                .retrieve()
                .body(SlotResponse.class);
    }

    public SlotResponse bookSlot(UUID slotId) {
        return restClient.post()
                .uri(schedulingServiceUrl + "/slots/{id}/book", slotId)
                .retrieve()
                .body(SlotResponse.class);
    }

    public SlotResponse releaseSlot(UUID slotId) {
        return restClient.post()
                .uri(schedulingServiceUrl + "/slots/{id}/release", slotId)
                .retrieve()
                .body(SlotResponse.class);
    }

    public WorkScheduleResponse getWorkScheduleById(UUID scheduleId) {
        return restClient.get()
                .uri(schedulingServiceUrl + "/work-schedules/{id}", scheduleId)
                .retrieve()
                .body(WorkScheduleResponse.class);
    }

    public ClinicResponse getClinicById(UUID clinicId) {
        return restClient.get()
                .uri(schedulingServiceUrl + "/clinics/{id}", clinicId)
                .retrieve()
                .body(ClinicResponse.class);
    }

    public RoomResponse getRoomById(UUID roomId) {
        return restClient.get()
                .uri(schedulingServiceUrl + "/rooms/{id}", roomId)
                .retrieve()
                .body(RoomResponse.class);
    }

    public SpecialtyResponse getSpecialtyById(UUID specialtyId) {
        return restClient.get()
                .uri(schedulingServiceUrl + "/specialties/{id}", specialtyId)
                .retrieve()
                .body(SpecialtyResponse.class);
    }

    public SlotResponse cancelBooking(UUID slotId) {
        return restClient.post()
                .uri(schedulingServiceUrl + "/slots/{id}/cancel-booking", slotId)
                .retrieve()
                .body(SlotResponse.class);
    }
}