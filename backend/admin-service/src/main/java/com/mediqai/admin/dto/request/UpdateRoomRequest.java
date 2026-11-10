package com.mediqai.admin.dto.request;

import com.mediqai.admin.dto.request.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {

    private String name;

    private UUID specialtyId;

    private String floor;
    private String area;

    private RoomStatus status;
}
