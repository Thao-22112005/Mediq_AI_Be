package com.mediq.scheduling.controller;

import com.mediq.scheduling.dto.request.CreateRoomRequest;
import com.mediq.scheduling.dto.request.UpdateRoomRequest;
import com.mediq.scheduling.dto.response.RoomResponse;
import com.mediq.scheduling.entity.RoomStatus;
import com.mediq.scheduling.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/clinics/{clinicId}/rooms")
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable("clinicId") UUID clinicId,
            @RequestBody CreateRoomRequest request
    ) {
        RoomResponse response = roomService.createRoom(clinicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/clinics/{clinicId}/rooms")
    public ResponseEntity<List<RoomResponse>> listRoomsByClinic(
            @PathVariable("clinicId") UUID clinicId,
            @RequestParam(value = "status", required = false) RoomStatus status
    ) {
        return ResponseEntity.ok(roomService.getRoomsByClinic(clinicId, status));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable("id") UUID roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @PatchMapping("/rooms/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable("id") UUID roomId,
            @RequestBody UpdateRoomRequest request
    ) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }
}
