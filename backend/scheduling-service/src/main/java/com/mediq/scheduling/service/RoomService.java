package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.request.CreateRoomRequest;
import com.mediq.scheduling.dto.request.UpdateRoomRequest;
import com.mediq.scheduling.dto.response.RoomResponse;
import com.mediq.scheduling.entity.RoomStatus;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    RoomResponse createRoom(UUID clinicId, CreateRoomRequest request);

    RoomResponse getRoomById(UUID roomId);

    List<RoomResponse> getRoomsByClinic(UUID clinicId, RoomStatus status);

    RoomResponse updateRoom(UUID roomId, UpdateRoomRequest request);
}
