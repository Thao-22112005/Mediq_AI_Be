package com.mediq.scheduling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediq.scheduling.dto.request.CreateWorkScheduleRequest;
import com.mediq.scheduling.dto.response.WorkScheduleResponse;
import com.mediq.scheduling.entity.WorkScheduleStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.exception.GlobalExceptionHandler;
import com.mediq.scheduling.service.WorkScheduleService;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkScheduleControllerTest {

    @Mock
    private WorkScheduleService workScheduleService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        WorkScheduleController controller = new WorkScheduleController(workScheduleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /work-schedules creates schedule and returns 201 Created")
    void createWorkSchedule_Success() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(12, 0);

        CreateWorkScheduleRequest request = CreateWorkScheduleRequest.builder()
                .doctorId(doctorId)
                .clinicId(clinicId)
                .specialtyId(specialtyId)
                .roomId(roomId)
                .date(date)
                .startTime(start)
                .endTime(end)
                .build();

        WorkScheduleResponse response = new WorkScheduleResponse(
                scheduleId, doctorId, clinicId, specialtyId, roomId,
                date, start, end, WorkScheduleStatus.PENDING, Instant.now(), Instant.now()
        );

        when(workScheduleService.createWorkSchedule(any(CreateWorkScheduleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/work-schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleId").value(scheduleId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /work-schedules/{id} returns 200 OK")
    void getWorkScheduleById_Success() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        WorkScheduleResponse response = new WorkScheduleResponse(
                scheduleId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(12, 0),
                WorkScheduleStatus.PENDING, Instant.now(), Instant.now()
        );

        when(workScheduleService.getWorkScheduleById(scheduleId)).thenReturn(response);

        mockMvc.perform(get("/work-schedules/{id}", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value(scheduleId.toString()));
    }

    @Test
    @DisplayName("POST /work-schedules/{id}/approve returns 200 OK")
    void approveWorkSchedule_Success() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        WorkScheduleResponse response = new WorkScheduleResponse(
                scheduleId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(12, 0),
                WorkScheduleStatus.APPROVED, Instant.now(), Instant.now()
        );

        when(workScheduleService.approveWorkSchedule(scheduleId)).thenReturn(response);

        mockMvc.perform(post("/work-schedules/{id}/approve", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /work-schedules/{id}/cancel returns 200 OK")
    void cancelWorkSchedule_Success() throws Exception {
        UUID scheduleId = UUID.randomUUID();
        WorkScheduleResponse response = new WorkScheduleResponse(
                scheduleId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(12, 0),
                WorkScheduleStatus.CANCELLED, Instant.now(), Instant.now()
        );

        when(workScheduleService.cancelWorkSchedule(scheduleId)).thenReturn(response);

        mockMvc.perform(post("/work-schedules/{id}/cancel", scheduleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
