
package com.mediqai.admin.controller;

import com.mediqai.admin.dto.request.CreateRoomRequest;
import com.mediqai.admin.dto.request.CreateWorkScheduleRequest;
import com.mediqai.admin.dto.request.UpdateRoomRequest;
import com.mediqai.admin.dto.response.DoctorResponse;
import com.mediqai.admin.dto.response.RoomResponse;
import com.mediqai.admin.dto.response.SpecialtyResponse;
import com.mediqai.admin.dto.response.WorkScheduleResponse;
import com.mediqai.admin.service.AdminRoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    private final AdminRoomService adminRoomService;

    public AdminRoomController(AdminRoomService adminRoomService) {
        this.adminRoomService = adminRoomService;
    }

    private String getToken(HttpServletRequest request) {
        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException(
                    "Thiếu hoặc sai định dạng Authorization token"
            );
        }

        return authorization.substring(7);
    }

    // GET /admin/rooms?clinicId={clinicId}&status=ACTIVE
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRoomsByClinic(
            @RequestParam UUID clinicId,
            @RequestParam(required = false) String status,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                adminRoomService.getRoomsByClinic(
                        clinicId,
                        status,
                        getToken(request)
                )
        );
    }

    // GET /admin/rooms/{roomId}
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(
            @PathVariable UUID roomId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                adminRoomService.getRoomById(
                        roomId,
                        getToken(request)
                )
        );
    }

    // POST /admin/rooms?clinicId={clinicId}
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @RequestParam UUID clinicId,
            @Valid @RequestBody CreateRoomRequest roomRequest,
            HttpServletRequest request
    ) {
        RoomResponse response = adminRoomService.createRoom(
                clinicId,
                roomRequest,
                getToken(request)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PATCH /admin/rooms/{roomId}
    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateRoomRequest roomRequest,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                adminRoomService.updateRoom(
                        roomId,
                        roomRequest,
                        getToken(request)
                )
        );
    }

    // GET /admin/rooms/{roomId}/specialty
    @GetMapping("/{roomId}/specialty")
    public ResponseEntity<SpecialtyResponse> getRoomSpecialty(
            @PathVariable UUID roomId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                adminRoomService.getRoomSpecialty(
                        roomId,
                        getToken(request)
                )
        );
    }

    // GET /admin/rooms/{roomId}/doctors
    @GetMapping("/{roomId}/doctors")
    public ResponseEntity<List<DoctorResponse>> getDoctorsByRoom(
            @PathVariable UUID roomId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                adminRoomService.getDoctorsByRoom(
                        roomId,
                        getToken(request)
                )
        );
    }

    @PostMapping("/{roomId}/assignments")
    public ResponseEntity<WorkScheduleResponse> assignDoctor(
            @PathVariable UUID roomId,
            @Valid @RequestBody CreateWorkScheduleRequest request,
            HttpServletRequest httpRequest
    ) {
        WorkScheduleResponse response = adminRoomService.assignDoctor(
                roomId,
                request,
                getToken(httpRequest)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/{roomId}/assignments")
    public ResponseEntity<List<WorkScheduleResponse>> getRoomAssignments(
            @PathVariable UUID roomId,
            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE
            )
            java.time.LocalDate date,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(
                adminRoomService.getRoomAssignments(
                        roomId,
                        date,
                        getToken(request)
                )
        );
    }


}