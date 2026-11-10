package com.mediq.scheduling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediq.scheduling.dto.request.CreateScheduleChangeRequest;
import com.mediq.scheduling.dto.request.ReviewScheduleChangeRequest;
import com.mediq.scheduling.dto.response.ScheduleChangeRequestResponse;
import com.mediq.scheduling.entity.ScheduleChangeRequestStatus;
import com.mediq.scheduling.exception.GlobalExceptionHandler;
import com.mediq.scheduling.service.ScheduleChangeRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScheduleChangeRequestControllerTest {

    @Mock
    private ScheduleChangeRequestService scheduleChangeRequestService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        ScheduleChangeRequestController controller = new ScheduleChangeRequestController(scheduleChangeRequestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /schedule-change-requests creates request and returns 201 Created")
    void createRequest_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        CreateScheduleChangeRequest request = CreateScheduleChangeRequest.builder()
                .scheduleId(scheduleId)
                .doctorId(doctorId)
                .requestedStartTime(LocalTime.of(9, 0))
                .requestedEndTime(LocalTime.of(13, 0))
                .reason("Doctor preference")
                .build();

        ScheduleChangeRequestResponse response = new ScheduleChangeRequestResponse(
                id, scheduleId, doctorId, LocalTime.of(9, 0), LocalTime.of(13, 0),
                null, "Doctor preference", ScheduleChangeRequestStatus.PENDING,
                null, null, Instant.now(), Instant.now()
        );

        when(scheduleChangeRequestService.createRequest(any(CreateScheduleChangeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/schedule-change-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /schedule-change-requests/{id}/approve returns 200 OK")
    void approveRequest_Success() throws Exception {
        UUID id = UUID.randomUUID();
        ReviewScheduleChangeRequest reviewRequest = new ReviewScheduleChangeRequest("admin-user", "Approved");

        ScheduleChangeRequestResponse response = new ScheduleChangeRequestResponse(
                id, UUID.randomUUID(), UUID.randomUUID(), LocalTime.of(9, 0), LocalTime.of(13, 0),
                null, "Doctor preference", ScheduleChangeRequestStatus.APPROVED,
                reviewRequest.getReviewedBy(), Instant.now(), Instant.now(), Instant.now()
        );

        when(scheduleChangeRequestService.approveRequest(eq(id), any(ReviewScheduleChangeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/schedule-change-requests/{id}/approve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
