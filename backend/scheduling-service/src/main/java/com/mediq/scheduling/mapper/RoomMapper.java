package com.mediq.scheduling.mapper;

import com.mediq.scheduling.dto.response.RoomResponse;
import com.mediq.scheduling.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "clinicId", source = "clinic.clinicId")
    @Mapping(target = "specialtyId", source = "specialty.specialtyId")
    RoomResponse toRoomResponse(Room room);

    List<RoomResponse> toRoomResponseList(List<Room> rooms);
}
