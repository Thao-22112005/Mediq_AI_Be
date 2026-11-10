package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateRoomRequest;
import com.mediq.scheduling.dto.request.UpdateRoomRequest;
import com.mediq.scheduling.dto.response.RoomResponse;
import com.mediq.scheduling.entity.Clinic;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ClinicRepository clinicRepository;
    private final SpecialtyRepository specialtyRepository;
    private final RoomMapper roomMapper;
    private final RoomValidator roomValidator;

    @Override
    @Transactional
    public RoomResponse createRoom(UUID clinicId, CreateRoomRequest request) {
        roomValidator.validateCreate(clinicId, request);

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLINIC_NOT_FOUND,
                        "Clinic not found with ID: " + clinicId));

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SPECIALTY_NOT_FOUND,
                        "Specialty not found with ID: " + request.getSpecialtyId()));

        if (specialty.getStatus() != SpecialtyStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.SPECIALTY_INACTIVE,
                    "Cannot assign inactive specialty to room");
        }

        String trimmedCode = request.getCode().trim();
        if (roomRepository.existsByClinic_ClinicIdAndCode(clinicId, trimmedCode)) {
            throw new BusinessException(ErrorCode.ROOM_CODE_ALREADY_EXISTS_IN_CLINIC,
                    "Room with code '" + trimmedCode + "' already exists in this clinic");
        }

        Room room = Room.builder()
                .clinic(clinic)
                .specialty(specialty)
                .code(trimmedCode)
                .name(request.getName().trim())
                .floor(request.getFloor() == null
                        ? null
                        : request.getFloor().trim())
                .area(request.getArea() == null
                        ? null
                        : request.getArea().trim())
                .status(RoomStatus.ACTIVE)
                .build();

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toRoomResponse(savedRoom);
    }

    @Override
    public RoomResponse getRoomById(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND,
                        "Room not found with ID: " + roomId));
        return roomMapper.toRoomResponse(room);
    }

    @Override
    public List<RoomResponse> getRoomsByClinic(UUID clinicId, RoomStatus status) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new BusinessException(ErrorCode.CLINIC_NOT_FOUND,
                    "Clinic not found with ID: " + clinicId);
        }

        List<Room> rooms = (status != null)
                ? roomRepository.findByClinic_ClinicIdAndStatus(clinicId, status)
                : roomRepository.findByClinic_ClinicId(clinicId);

        return roomMapper.toRoomResponseList(rooms);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request) {
        roomValidator.validateUpdate(request);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND,
                        "Room not found with ID: " + roomId));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            room.setName(request.getName().trim());
        }

        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SPECIALTY_NOT_FOUND,
                            "Specialty not found with ID: " + request.getSpecialtyId()));

            if (specialty.getStatus() != SpecialtyStatus.ACTIVE) {
                throw new BusinessException(ErrorCode.SPECIALTY_INACTIVE,
                        "Cannot assign inactive specialty to room");
            }
            room.setSpecialty(specialty);
        }

        if (request.getFloor() != null) {
            room.setFloor(request.getFloor().trim());
        }

        if (request.getArea() != null) {
            room.setArea(request.getArea().trim());
        }

        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        Room updatedRoom = roomRepository.save(room);
        return roomMapper.toRoomResponse(updatedRoom);
    }
}
