package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateSpecialtyRequest;
import com.mediq.scheduling.dto.request.UpdateSpecialtyRequest;
import com.mediq.scheduling.dto.response.SpecialtyResponse;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.SpecialtyStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.SpecialtyMapper;
import com.mediq.scheduling.repository.SpecialtyRepository;
import com.mediq.scheduling.validation.SpecialtyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private SpecialtyMapper specialtyMapper;

    private SpecialtyValidator specialtyValidator;
    private SpecialtyServiceImpl specialtyService;

    @BeforeEach
    void setUp() {
        specialtyValidator = new SpecialtyValidator();
        specialtyService = new SpecialtyServiceImpl(
                specialtyRepository,
                specialtyMapper,
                specialtyValidator
        );
    }

    @Test
    @DisplayName("Should create specialty successfully with valid data")
    void shouldCreateSpecialtySuccessfully() {
        CreateSpecialtyRequest request = CreateSpecialtyRequest.builder()
                .name("Cardiology")
                .description("Heart and cardiovascular diseases")
                .defaultSlotDuration(30)
                .build();

        when(specialtyRepository.existsByName("Cardiology")).thenReturn(false);

        UUID specialtyId = UUID.randomUUID();
        Specialty savedSpecialty = Specialty.builder()
                .specialtyId(specialtyId)
                .name("Cardiology")
                .description("Heart and cardiovascular diseases")
                .defaultSlotDuration(30)
                .status(SpecialtyStatus.ACTIVE)
                .build();

        when(specialtyRepository.save(any(Specialty.class))).thenReturn(savedSpecialty);

        SpecialtyResponse expectedResponse = new SpecialtyResponse(
                specialtyId, "Cardiology", "Heart and cardiovascular diseases",
                SpecialtyStatus.ACTIVE, 30, Instant.now(), Instant.now()
        );
        when(specialtyMapper.toSpecialtyResponse(savedSpecialty)).thenReturn(expectedResponse);

        SpecialtyResponse response = specialtyService.createSpecialty(request);

        assertNotNull(response);
        assertEquals("Cardiology", response.name());
        assertEquals(30, response.defaultSlotDuration());
        verify(specialtyRepository).save(any(Specialty.class));
    }

    @Test
    @DisplayName("Should reject specialty creation when name already exists")
    void shouldRejectWhenSpecialtyNameExists() {
        CreateSpecialtyRequest request = CreateSpecialtyRequest.builder()
                .name("Cardiology")
                .build();

        when(specialtyRepository.existsByName("Cardiology")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> specialtyService.createSpecialty(request));
        assertEquals(ErrorCode.SPECIALTY_NAME_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should update specialty successfully")
    void shouldUpdateSpecialtySuccessfully() {
        UUID specialtyId = UUID.randomUUID();
        Specialty existingSpecialty = Specialty.builder()
                .specialtyId(specialtyId)
                .name("Dermatology")
                .defaultSlotDuration(15)
                .status(SpecialtyStatus.ACTIVE)
                .build();

        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(existingSpecialty));
        when(specialtyRepository.existsByNameAndSpecialtyIdNot("Skin Care", specialtyId)).thenReturn(false);

        Specialty updatedSpecialty = Specialty.builder()
                .specialtyId(specialtyId)
                .name("Skin Care")
                .defaultSlotDuration(20)
                .status(SpecialtyStatus.ACTIVE)
                .build();

        when(specialtyRepository.save(any(Specialty.class))).thenReturn(updatedSpecialty);

        SpecialtyResponse expectedResponse = new SpecialtyResponse(
                specialtyId, "Skin Care", null, SpecialtyStatus.ACTIVE, 20, Instant.now(), Instant.now()
        );
        when(specialtyMapper.toSpecialtyResponse(updatedSpecialty)).thenReturn(expectedResponse);

        UpdateSpecialtyRequest request = UpdateSpecialtyRequest.builder()
                .name("Skin Care")
                .defaultSlotDuration(20)
                .build();

        SpecialtyResponse response = specialtyService.updateSpecialty(specialtyId, request);

        assertNotNull(response);
        assertEquals("Skin Care", response.name());
        assertEquals(20, response.defaultSlotDuration());
    }
}
