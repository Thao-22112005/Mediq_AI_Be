package com.mediq.scheduling.repository;

import com.mediq.scheduling.entity.Room;
import com.mediq.scheduling.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByClinic_ClinicId(UUID clinicId);

    List<Room> findByClinic_ClinicIdAndStatus(UUID clinicId, RoomStatus status);

    List<Room> findBySpecialty_SpecialtyId(UUID specialtyId);

    boolean existsByClinic_ClinicIdAndCode(UUID clinicId, String code);

    boolean existsByClinic_ClinicIdAndCodeAndRoomIdNot(UUID clinicId, String code, UUID roomId);
}
