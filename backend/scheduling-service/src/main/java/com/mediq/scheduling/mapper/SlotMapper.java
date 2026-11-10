package com.mediq.scheduling.mapper;

import com.mediq.scheduling.dto.response.SlotResponse;
import com.mediq.scheduling.entity.Slot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SlotMapper {

    @Mapping(target = "scheduleId", source = "workSchedule.scheduleId")
    @Mapping(target = "doctorId", source = "workSchedule.doctorId")
    @Mapping(target = "clinicId", source = "workSchedule.clinic.clinicId")
    @Mapping(target = "specialtyId", source = "workSchedule.specialty.specialtyId")
    @Mapping(target = "roomId", source = "workSchedule.room.roomId")
    @Mapping(target = "date", source = "workSchedule.date")
    SlotResponse toSlotResponse(Slot slot);

    List<SlotResponse> toSlotResponseList(List<Slot> slots);
}
