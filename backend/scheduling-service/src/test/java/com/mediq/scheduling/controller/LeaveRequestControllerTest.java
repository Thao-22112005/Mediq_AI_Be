package com.mediq.scheduling.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediq.scheduling.dto.request.CreateLeaveRequest;
import com.mediq.scheduling.dto.request.ReviewLeaveRequest;
import com.mediq.scheduling.dto.response.ApprovedLeaveResult;
import com.mediq.scheduling.dto.response.LeaveRequestResponse;
import com.mediq.scheduling.entity.LeaveRequestStatus;
import com.mediq.scheduling.exception.GlobalExceptionHandler;
import com.mediq.scheduling.service.LeaveRequestService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeaveRequestControllerTest {

    @Mock
    private LeaveRequestService leaveRequestService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        LeaveRequestController controller = new LeaveRequestController(leaveRequestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /leave-requests creates request and returns 201 Created")
    void createLeaveRequest_Success() throws Exception {
        UUID leaveRequestId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(5);
        CreateLeaveRequest request = new CreateLeaveRequest(doctorId, start, end, "Annual Leave");

        LeaveRequestResponse response = new LeaveRequestResponse(
                leaveRequestId, doctorId, start, end, "Annual Leave",
                LeaveRequestStatus.PENDING, null, null, Instant.now(), Instant.now()
        );

        when(leaveRequestService.createLeaveRequest(any(CreateLeaveRequest.class))).thenReturn(response);

        mockMvc.perform(post("/leave-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveRequestId").value(leaveRequestId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /leave-requests/{id} returns 200 OK")
    void getLeaveRequestById_Success() throws Exception {
        UUID leaveRequestId = UUID.randomUUID();
        LeaveRequestResponse response = new LeaveRequestResponse(
                leaveRequestId, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Vacation",
                LeaveRequestStatus.PENDING, null, null, Instant.now(), Instant.now()
        );

        when(leaveRequestService.getLeaveRequestById(leaveRequestId)).thenReturn(response);

        mockMvc.perform(get("/leave-requests/{id}", leaveRequestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveRequestId").value(leaveRequestId.toString()));
    }

    @Test
    @DisplayName("POST /leave-requests/{id}/approve returns 200 OK with ApprovedLeaveResult")
    void approveLeaveRequest_Success() throws Exception {
        UUID leaveRequestId = UUID.randomUUID();
        ReviewLeaveRequest reviewRequest = new ReviewLeaveRequest("admin", "Approved");
        LeaveRequestResponse leaveResp = new LeaveRequestResponse(
                leaveRequestId, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Vacation",
                LeaveRequestStatus.APPROVED, "admin", Instant.now(), Instant.now(), Instant.now()
        );
        ApprovedLeaveResult result = new ApprovedLeaveResult(leaveResp, List.of(), 1, 0, 0);

        when(leaveRequestService.approveLeaveRequest(eq(leaveRequestId), any(ReviewLeaveRequest.class)))
                .thenReturn(result);

        mockMvc.perform(post("/leave-requests/{id}/approve", leaveRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveRequest.status").value("APPROVED"))
                .andExpect(jsonPath("$.cancelledSchedulesCount").value(1));
    }
}
