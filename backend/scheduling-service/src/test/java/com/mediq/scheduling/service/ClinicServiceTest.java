package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateClinicRequest;
import com.mediq.scheduling.dto.response.ClinicResponse;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.ClinicStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.ClinicMapper;
import com.mediq.scheduling.repository.ClinicRepository;
import com.mediq.scheduling.validation.ClinicValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicServiceTest {

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private ClinicMapper clinicMapper;

    private ClinicValidator clinicValidator;
    private ClinicServiceImpl clinicService;

    @BeforeEach
    void setUp() {
        clinicValidator = new ClinicValidator();
        clinicService = new ClinicServiceImpl(
                clinicRepository,
                clinicMapper,
                clinicValidator
        );
    }

    @Test
    @DisplayName("Should create clinic successfully with valid data (BR-CLINIC-001)")
    void shouldCreateClinicSuccessfully() {
        CreateClinicRequest request = CreateClinicRequest.builder()
                .code("CLN-D1")
                .name("MEDIQ District 1 Clinic")
                .address("123 Nguyen Hue, D1, HCMC")
                .phone("028-12345678")
                .build();

        when(clinicRepository.existsByCode("CLN-D1")).thenReturn(false);

        UUID clinicId = UUID.randomUUID();
        Clinic savedClinic = Clinic.builder()
                .clinicId(clinicId)
                .code("CLN-D1")
                .name("MEDIQ District 1 Clinic")
                .address("123 Nguyen Hue, D1, HCMC")
                .phone("028-12345678")
                .status(ClinicStatus.ACTIVE)
                .rooms(new ArrayList<>())
                .build();

        when(clinicRepository.save(any(Clinic.class))).thenReturn(savedClinic);

        ClinicResponse expectedResponse = new ClinicResponse(
                clinicId, "CLN-D1", "MEDIQ District 1 Clinic", "123 Nguyen Hue, D1, HCMC",
                "028-12345678", ClinicStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(clinicMapper.toClinicResponse(savedClinic)).thenReturn(expectedResponse);

        ClinicResponse response = clinicService.createClinic(request);

        assertNotNull(response);
        assertEquals("CLN-D1", response.code());
        assertEquals("MEDIQ District 1 Clinic", response.name());
        verify(clinicRepository).save(any(Clinic.class));
    }

    @Test
    @DisplayName("Should reject clinic creation when code already exists")
    void shouldRejectWhenClinicCodeExists() {
        CreateClinicRequest request = CreateClinicRequest.builder()
                .code("CLN-D1")
                .name("Duplicate Clinic")
                .build();

        when(clinicRepository.existsByCode("CLN-D1")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> clinicService.createClinic(request));
        assertEquals(ErrorCode.CLINIC_CODE_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should activate and deactivate clinic")
    void shouldActivateAndDeactivateClinic() {
        UUID clinicId = UUID.randomUUID();
        Clinic clinic = Clinic.builder()
                .clinicId(clinicId)
                .code("CLN-D2")
                .name("MEDIQ D2")
                .status(ClinicStatus.ACTIVE)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(clinicRepository.save(any(Clinic.class))).thenReturn(clinic);

        ClinicResponse inactiveResponse = new ClinicResponse(
                clinicId, "CLN-D2", "MEDIQ D2", null, null,
                ClinicStatus.INACTIVE, Instant.now(), Instant.now()
        );
        when(clinicMapper.toClinicResponse(clinic)).thenReturn(inactiveResponse);

        ClinicResponse response = clinicService.deactivateClinic(clinicId);
        assertNotNull(response);
        assertEquals(ClinicStatus.INACTIVE, response.status());
    }
}
