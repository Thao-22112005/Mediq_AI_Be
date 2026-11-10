package com.mediq.doctor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediq.doctor.dto.request.AddDoctorSpecialtyRequest;
import com.mediq.doctor.dto.request.CreateDoctorRequest;
import com.mediq.doctor.dto.request.UpdateDoctorRequest;
import com.mediq.doctor.dto.response.DoctorResponse;
import com.mediq.doctor.dto.response.DoctorSpecialtyResponse;
import com.mediq.doctor.entity.DoctorStatus;
import com.mediq.doctor.exception.BusinessException;
import com.mediq.doctor.exception.ErrorCode;
import com.mediq.doctor.exception.GlobalExceptionHandler;
import com.mediq.doctor.service.DoctorService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        DoctorController controller = new DoctorController(doctorService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /doctors creates doctor successfully and returns 201")
    void createDoctor_Success() throws Exception {
        UUID doctorId = UUID.randomUUID();
        Long userId = 1L;
        CreateDoctorRequest request = CreateDoctorRequest.builder()
                .userId(userId)
                .fullName("Dr. John Watson")
                .phoneNumber("0987654321")
                .licenseNumber("DOC-12345")
                .specialties(List.of())
                .build();

        DoctorResponse response = new DoctorResponse(
                doctorId, userId, "Dr. John Watson", "0987654321", "DOC-12345",
                DoctorStatus.ACTIVE, List.of(), Instant.now(), Instant.now()
        );

        when(doctorService.createDoctor(any(CreateDoctorRequest.class))).thenReturn(response);

        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.fullName").value("Dr. John Watson"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /doctors/{id} returns 200 OK when doctor exists")
    void getDoctorById_Success() throws Exception {
        UUID doctorId = UUID.randomUUID();
        DoctorResponse response = new DoctorResponse(
                doctorId, 2L, "Dr. Gregory House", "0987654322", "DOC-99999",
                DoctorStatus.ACTIVE, List.of(), Instant.now(), Instant.now()
        );

        when(doctorService.getDoctorById(doctorId)).thenReturn(response);

        mockMvc.perform(get("/doctors/{id}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(doctorId.toString()))
                .andExpect(jsonPath("$.fullName").value("Dr. Gregory House"));
    }

    @Test
    @DisplayName("GET /doctors/{id} returns 404 Not Found when doctor does not exist")
    void getDoctorById_NotFound() throws Exception {
        UUID doctorId = UUID.randomUUID();
        when(doctorService.getDoctorById(doctorId))
                .thenThrow(new BusinessException(ErrorCode.DOCTOR_NOT_FOUND, "Doctor not found"));

        mockMvc.perform(get("/doctors/{id}", doctorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DOCTOR_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /doctors returns 200 OK with doctor list")
    void listDoctors_Success() throws Exception {
        UUID doctorId = UUID.randomUUID();
        DoctorResponse doc = new DoctorResponse(
                doctorId, 3L, "Dr. Strange", "0987654323", "DOC-777",
                DoctorStatus.ACTIVE, List.of(), Instant.now(), Instant.now()
        );

        when(doctorService.getAllDoctors(DoctorStatus.ACTIVE)).thenReturn(List.of(doc));

        mockMvc.perform(get("/doctors").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Dr. Strange"));
    }

    @Test
    @DisplayName("PATCH /doctors/{id} updates doctor and returns 200 OK")
    void updateDoctor_Success() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UpdateDoctorRequest request = UpdateDoctorRequest.builder()
                .fullName("Dr. Stephen Strange")
                .phoneNumber("0987654324")
                .licenseNumber("DOC-888")
                .status(DoctorStatus.ACTIVE)
                .specialties(List.of())
                .build();

        DoctorResponse response = new DoctorResponse(
                doctorId, 4L, "Dr. Stephen Strange", "0987654324", "DOC-888",
                DoctorStatus.ACTIVE, List.of(), Instant.now(), Instant.now()
        );

        when(doctorService.updateDoctor(eq(doctorId), any(UpdateDoctorRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/doctors/{id}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Stephen Strange"));
    }

    @Test
    @DisplayName("POST /doctors/{id}/specialties adds specialty and returns 201")
    void addSpecialty_Success() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();
        AddDoctorSpecialtyRequest request = new AddDoctorSpecialtyRequest(specialtyId, true);

        DoctorSpecialtyResponse response = new DoctorSpecialtyResponse(
                UUID.randomUUID(), doctorId, specialtyId, true, Instant.now()
        );

        when(doctorService.addSpecialty(eq(doctorId), any(AddDoctorSpecialtyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/doctors/{id}/specialties", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.specialtyId").value(specialtyId.toString()))
                .andExpect(jsonPath("$.isPrimary").value(true));
    }

    @Test
    @DisplayName("DELETE /doctors/{id}/specialties/{specialtyId} returns 204 No Content")
    void removeSpecialty_Success() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        mockMvc.perform(delete("/doctors/{id}/specialties/{specialtyId}", doctorId, specialtyId))
                .andExpect(status().isNoContent());

        verify(doctorService).removeSpecialty(doctorId, specialtyId);
    }
}
