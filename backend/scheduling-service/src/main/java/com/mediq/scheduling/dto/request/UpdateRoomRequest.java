package com.mediq.scheduling.dto.request;

import com.mediq.scheduling.entity.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomRequest {

    private String name;
    private UUID specialtyId;
    private String floor;
    private String area;
    private RoomStatus status;
}
