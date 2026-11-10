package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.response.SlotResponse;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.exception.GlobalExceptionHandler;
import com.mediq.scheduling.service.SlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SlotControllerTest {

    @Mock
    private SlotService slotService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SlotController controller = new SlotController(slotService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /slots/{id} returns 200 OK when slot exists")
    void getSlotById_Success() throws Exception {
        UUID slotId = UUID.randomUUID();
        SlotResponse response = new SlotResponse(
                slotId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                SlotStatus.AVAILABLE, Instant.now(), Instant.now()
        );

        when(slotService.getSlotById(slotId)).thenReturn(response);

        mockMvc.perform(get("/slots/{id}", slotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").value(slotId.toString()))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST /slots/{id}/hold returns 200 OK on success")
    void holdSlot_Success() throws Exception {
        UUID slotId = UUID.randomUUID();
        SlotResponse response = new SlotResponse(
                slotId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                SlotStatus.HELD, Instant.now(), Instant.now()
        );

        when(slotService.holdSlot(slotId)).thenReturn(response);

        mockMvc.perform(post("/slots/{id}/hold", slotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HELD"));
    }

    @Test
    @DisplayName("POST /slots/{id}/hold returns 409 Conflict when slot is already held")
    void holdSlot_Conflict() throws Exception {
        UUID slotId = UUID.randomUUID();
        when(slotService.holdSlot(slotId))
                .thenThrow(new BusinessException(ErrorCode.SLOT_NOT_AVAILABLE, "Slot is not available for holding"));

        mockMvc.perform(post("/slots/{id}/hold", slotId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SLOT_NOT_AVAILABLE"));
    }

    @Test
    @DisplayName("POST /slots/{id}/book returns 200 OK on success")
    void bookSlot_Success() throws Exception {
        UUID slotId = UUID.randomUUID();
        SlotResponse response = new SlotResponse(
                slotId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                SlotStatus.BOOKED, Instant.now(), Instant.now()
        );

        when(slotService.bookSlot(slotId)).thenReturn(response);

        mockMvc.perform(post("/slots/{id}/book", slotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    @DisplayName("POST /slots/{id}/release returns 200 OK on success")
    void releaseSlot_Success() throws Exception {
        UUID slotId = UUID.randomUUID();
        SlotResponse response = new SlotResponse(
                slotId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                SlotStatus.AVAILABLE, Instant.now(), Instant.now()
        );

        when(slotService.releaseSlot(slotId)).thenReturn(response);

        mockMvc.perform(post("/slots/{id}/release", slotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }
}
