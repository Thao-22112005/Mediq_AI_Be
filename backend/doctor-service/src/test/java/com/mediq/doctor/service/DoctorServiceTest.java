package com.mediq.doctor.service;

import com.mediq.doctor.dto.request.AddDoctorSpecialtyRequest;
import com.mediq.doctor.dto.request.CreateDoctorRequest;
import com.mediq.doctor.dto.response.DoctorResponse;
import com.mediq.doctor.dto.response.DoctorSpecialtyResponse;
import com.mediq.doctor.entity.Doctor;
import com.mediq.doctor.entity.DoctorSpecialty;
import com.mediq.doctor.entity.DoctorStatus;
import com.mediq.doctor.exception.BusinessException;
import com.mediq.doctor.exception.ErrorCode;
import com.mediq.doctor.mapper.DoctorMapper;
import com.mediq.doctor.repository.DoctorRepository;
import com.mediq.doctor.repository.DoctorSpecialtyRepository;
import com.mediq.doctor.validation.DoctorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorSpecialtyRepository doctorSpecialtyRepository;

    @Mock
    private DoctorMapper doctorMapper;

    private DoctorValidator doctorValidator;
    private DoctorServiceImpl doctorService;

    @BeforeEach
    void setUp() {
        doctorValidator = new DoctorValidator();
        doctorService = new DoctorServiceImpl(
                doctorRepository,
                doctorSpecialtyRepository,
                doctorMapper,
                doctorValidator
        );
    }

    @Test
    @DisplayName("Should create doctor successfully with valid data")
    void shouldCreateDoctorSuccessfully() {
        Long userId = 1L;
        UUID specialtyId = UUID.randomUUID();

        CreateDoctorRequest request = CreateDoctorRequest.builder()
                .userId(userId)
                .fullName("Dr. Nguyen Van A")
                .phoneNumber("0987654321")
                .licenseNumber("MED-12345")
                .specialties(List.of(
                        new CreateDoctorRequest.InitialSpecialtyRequest(specialtyId, true)
                ))
                .build();

        when(doctorRepository.existsByLicenseNumber("MED-12345")).thenReturn(false);
        when(doctorRepository.existsByUserId(userId)).thenReturn(false);

        UUID doctorId = UUID.randomUUID();
        Doctor savedDoctor = Doctor.builder()
                .doctorId(doctorId)
                .userId(userId)
                .fullName("Dr. Nguyen Van A")
                .phoneNumber("0987654321")
                .licenseNumber("MED-12345")
                .status(DoctorStatus.ACTIVE)
                .specialties(new ArrayList<>())
                .build();

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);

        DoctorResponse expectedResponse = new DoctorResponse(
                doctorId, userId, "Dr. Nguyen Van A", "0987654321", "MED-12345",
                DoctorStatus.ACTIVE, List.of(), Instant.now(), Instant.now()
        );
        when(doctorMapper.toDoctorResponse(savedDoctor)).thenReturn(expectedResponse);

        DoctorResponse result = doctorService.createDoctor(request);

        assertNotNull(result);
        assertEquals("Dr. Nguyen Van A", result.fullName());
        assertEquals("MED-12345", result.licenseNumber());
        verify(doctorRepository).save(any(Doctor.class));
    }

    @Test
    @DisplayName("Should reject doctor creation when license number already exists")
    void shouldRejectWhenLicenseNumberExists() {
        CreateDoctorRequest request = CreateDoctorRequest.builder()
                .userId(2L)
                .fullName("Dr. Duplicate")
                .licenseNumber("DUPLICATE-001")
                .build();

        when(doctorRepository.existsByLicenseNumber("DUPLICATE-001")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> doctorService.createDoctor(request));
        assertEquals(ErrorCode.LICENSE_NUMBER_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject doctor creation when having more than one primary specialty (BR-DOCTOR-001)")
    void shouldRejectMultiplePrimarySpecialties() {
        CreateDoctorRequest request = CreateDoctorRequest.builder()
                .userId(3L)
                .fullName("Dr. Multi Primary")
                .licenseNumber("MULTI-001")
                .specialties(List.of(
                        new CreateDoctorRequest.InitialSpecialtyRequest(UUID.randomUUID(), true),
                        new CreateDoctorRequest.InitialSpecialtyRequest(UUID.randomUUID(), true)
                ))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> doctorService.createDoctor(request));
        assertEquals(ErrorCode.PRIMARY_SPECIALTY_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should add specialty to doctor successfully")
    void shouldAddSpecialtySuccessfully() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        Doctor doctor = Doctor.builder()
                .doctorId(doctorId)
                .fullName("Dr. Specialist")
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorSpecialtyRepository.existsByDoctor_DoctorIdAndSpecialtyId(doctorId, specialtyId)).thenReturn(false);
        when(doctorSpecialtyRepository.findByDoctor_DoctorIdAndIsPrimaryTrue(doctorId)).thenReturn(Optional.empty());

        UUID doctorSpecialtyId = UUID.randomUUID();
        DoctorSpecialty savedSpecialty = DoctorSpecialty.builder()
                .doctorSpecialtyId(doctorSpecialtyId)
                .doctor(doctor)
                .specialtyId(specialtyId)
                .isPrimary(true)
                .build();

        when(doctorSpecialtyRepository.save(any(DoctorSpecialty.class))).thenReturn(savedSpecialty);

        DoctorSpecialtyResponse expectedResponse = new DoctorSpecialtyResponse(
                doctorSpecialtyId, doctorId, specialtyId, true, Instant.now()
        );
        when(doctorMapper.toSpecialtyResponse(savedSpecialty)).thenReturn(expectedResponse);

        AddDoctorSpecialtyRequest request = new AddDoctorSpecialtyRequest(specialtyId, true);
        DoctorSpecialtyResponse response = doctorService.addSpecialty(doctorId, request);

        assertNotNull(response);
        assertEquals(specialtyId, response.specialtyId());
        assertEquals(true, response.isPrimary());
    }

    @Test
    @DisplayName("Should reject adding duplicate primary specialty (BR-DOCTOR-001)")
    void shouldRejectSecondPrimarySpecialty() {
        UUID doctorId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        Doctor doctor = Doctor.builder()
                .doctorId(doctorId)
                .fullName("Dr. Specialist")
                .build();

        DoctorSpecialty existingPrimary = DoctorSpecialty.builder()
                .doctorSpecialtyId(UUID.randomUUID())
                .doctor(doctor)
                .specialtyId(UUID.randomUUID())
                .isPrimary(true)
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorSpecialtyRepository.existsByDoctor_DoctorIdAndSpecialtyId(doctorId, specialtyId)).thenReturn(false);
        when(doctorSpecialtyRepository.findByDoctor_DoctorIdAndIsPrimaryTrue(doctorId)).thenReturn(Optional.of(existingPrimary));

        AddDoctorSpecialtyRequest request = new AddDoctorSpecialtyRequest(specialtyId, true);

        BusinessException ex = assertThrows(BusinessException.class, () -> doctorService.addSpecialty(doctorId, request));
        assertEquals(ErrorCode.PRIMARY_SPECIALTY_ALREADY_EXISTS, ex.getErrorCode());
    }
}
