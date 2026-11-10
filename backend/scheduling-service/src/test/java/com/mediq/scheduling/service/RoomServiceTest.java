package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateRoomRequest;
import com.mediq.scheduling.dto.response.RoomResponse;
import com.mediq.scheduling.entity.Clinic;
import com.mediq.scheduling.entity.ClinicStatus;
import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.RoomStatus;
import com.mediq.scheduling.entity.Specialty;
import com.mediq.scheduling.entity.SpecialtyStatus;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.RoomMapper;
import com.mediq.scheduling.repository.ClinicRepository;
import com.mediq.scheduling.repository.RoomRepository;
import com.mediq.scheduling.repository.SpecialtyRepository;
import com.mediq.scheduling.validation.RoomValidator;
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
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private RoomMapper roomMapper;

    private RoomValidator roomValidator;
    private RoomServiceImpl roomService;

    @BeforeEach
    void setUp() {
        roomValidator = new RoomValidator();
        roomService = new RoomServiceImpl(
                roomRepository,
                clinicRepository,
                specialtyRepository,
                roomMapper,
                roomValidator
        );
    }

    @Test
    @DisplayName("Should create room successfully belonging to Clinic (BR-CLINIC-002) and serving Specialty (BR-CLINIC-003)")
    void shouldCreateRoomSuccessfully() {
        UUID clinicId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        Clinic clinic = Clinic.builder()
                .clinicId(clinicId)
                .code("CLN-01")
                .name("Clinic 1")
                .status(ClinicStatus.ACTIVE)
                .build();

        Specialty specialty = Specialty.builder()
                .specialtyId(specialtyId)
                .name("Cardiology")
                .status(SpecialtyStatus.ACTIVE)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.existsByClinic_ClinicIdAndCode(clinicId, "R-101")).thenReturn(false);

        UUID roomId = UUID.randomUUID();
        Room savedRoom = Room.builder()
                .roomId(roomId)
                .clinic(clinic)
                .specialty(specialty)
                .code("R-101")
                .name("Examination Room 101")
                .status(RoomStatus.ACTIVE)
                .build();

        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        RoomResponse expectedResponse = new RoomResponse(
                roomId, clinicId, specialtyId, "R-101", "Examination Room 101",
                RoomStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(roomMapper.toRoomResponse(savedRoom)).thenReturn(expectedResponse);

        CreateRoomRequest request = CreateRoomRequest.builder()
                .code("R-101")
                .name("Examination Room 101")
                .specialtyId(specialtyId)
                .build();

        RoomResponse response = roomService.createRoom(clinicId, request);

        assertNotNull(response);
        assertEquals("R-101", response.code());
        assertEquals(clinicId, response.clinicId());
        assertEquals(specialtyId, response.specialtyId());
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    @DisplayName("Should reject room creation when clinic not found")
    void shouldRejectWhenClinicNotFound() {
        UUID clinicId = UUID.randomUUID();
        CreateRoomRequest request = CreateRoomRequest.builder()
                .code("R-101")
                .name("Room 101")
                .specialtyId(UUID.randomUUID())
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> roomService.createRoom(clinicId, request));
        assertEquals(ErrorCode.CLINIC_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject room creation when specialty not found")
    void shouldRejectWhenSpecialtyNotFound() {
        UUID clinicId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        Clinic clinic = Clinic.builder()
                .clinicId(clinicId)
                .code("CLN-01")
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.empty());

        CreateRoomRequest request = CreateRoomRequest.builder()
                .code("R-101")
                .name("Room 101")
                .specialtyId(specialtyId)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> roomService.createRoom(clinicId, request));
        assertEquals(ErrorCode.SPECIALTY_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject room creation when specialty is inactive")
    void shouldRejectWhenSpecialtyInactive() {
        UUID clinicId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        Clinic clinic = Clinic.builder()
                .clinicId(clinicId)
                .code("CLN-01")
                .build();

        Specialty inactiveSpecialty = Specialty.builder()
                .specialtyId(specialtyId)
                .name("Old Specialty")
                .status(SpecialtyStatus.INACTIVE)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(inactiveSpecialty));

        CreateRoomRequest request = CreateRoomRequest.builder()
                .code("R-101")
                .name("Room 101")
                .specialtyId(specialtyId)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> roomService.createRoom(clinicId, request));
        assertEquals(ErrorCode.SPECIALTY_INACTIVE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should reject room creation when code already exists in clinic")
    void shouldRejectWhenRoomCodeExistsInClinic() {
        UUID clinicId = UUID.randomUUID();
        UUID specialtyId = UUID.randomUUID();

        Clinic clinic = Clinic.builder()
                .clinicId(clinicId)
                .code("CLN-01")
                .build();

        Specialty specialty = Specialty.builder()
                .specialtyId(specialtyId)
                .status(SpecialtyStatus.ACTIVE)
                .build();

        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(roomRepository.existsByClinic_ClinicIdAndCode(clinicId, "R-101")).thenReturn(true);

        CreateRoomRequest request = CreateRoomRequest.builder()
                .code("R-101")
                .name("Duplicate Room")
                .specialtyId(specialtyId)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> roomService.createRoom(clinicId, request));
        assertEquals(ErrorCode.ROOM_CODE_ALREADY_EXISTS_IN_CLINIC, ex.getErrorCode());
    }
}
