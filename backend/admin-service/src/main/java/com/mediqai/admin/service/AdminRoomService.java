
package com.mediqai.admin.service;

import com.mediqai.admin.client.DoctorClient;
import com.mediqai.admin.client.RoomClient;
import com.mediqai.admin.client.SpecialtyClient;
import com.mediqai.admin.client.WorkScheduleClient;
import com.mediqai.admin.dto.request.CreateRoomRequest;
import com.mediqai.admin.dto.request.CreateWorkScheduleRequest;
import com.mediqai.admin.dto.request.UpdateRoomRequest;
import com.mediqai.admin.dto.response.DoctorResponse;
import com.mediqai.admin.dto.response.RoomResponse;
import com.mediqai.admin.dto.response.SpecialtyResponse;
import com.mediqai.admin.dto.response.WorkScheduleResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminRoomService {

    private final RoomClient roomClient;
    private final SpecialtyClient specialtyClient;
    private final DoctorClient doctorClient;
    private final WorkScheduleClient workScheduleClient;

    public AdminRoomService(
            RoomClient roomClient,
            SpecialtyClient specialtyClient,
            DoctorClient doctorClient,
            WorkScheduleClient workScheduleClient
    ) {
        this.roomClient = roomClient;
        this.specialtyClient = specialtyClient;
        this.doctorClient = doctorClient;
        this.workScheduleClient = workScheduleClient;
    }

    public List<RoomResponse> getRoomsByClinic(
            UUID clinicId,
            String status,
            String token
    ) {
        return roomClient.getRoomsByClinic(clinicId, status, token);
    }

    public RoomResponse getRoomById(UUID roomId, String token) {
        return roomClient.getRoomById(roomId, token);
    }

    public RoomResponse createRoom(
            UUID clinicId,
            CreateRoomRequest request,
            String token
    ) {
        return roomClient.createRoom(clinicId, request, token);
    }

    public RoomResponse updateRoom(
            UUID roomId,
            UpdateRoomRequest request,
            String token
    ) {
        return roomClient.updateRoom(roomId, request, token);
    }

    public SpecialtyResponse getRoomSpecialty(
            UUID roomId,
            String token
    ) {
        RoomResponse room = roomClient.getRoomById(roomId, token);

        if (room.getSpecialtyId() == null) {
            return null;
        }

        return specialtyClient.getSpecialtyById(
                room.getSpecialtyId(),
                token
        );
    }


    public List<DoctorResponse> getDoctorsByRoom(
            UUID roomId,
            String token
    ) {
        // 1. Lấy thông tin phòng
        RoomResponse room = roomClient.getRoomById(roomId, token);

        // 2. Kiểm tra phòng đã được cấu hình chuyên khoa chưa
        if (room.getSpecialtyId() == null) {
            throw new IllegalArgumentException(
                    "Phòng chưa được cấu hình chuyên khoa"
            );
        }

        UUID roomSpecialtyId = room.getSpecialtyId();

        // 3. Lấy danh sách bác sĩ
        List<DoctorResponse> doctors =
                doctorClient.getAllDoctors(null, token);

        if (doctors == null) {
            return List.of();
        }

        // 4. Lọc bác sĩ có chuyên khoa phù hợp với phòng
        return doctors.stream()
                .filter(doctor ->
                        doctor.getSpecialties() != null
                                && doctor.getSpecialties().stream()
                                .anyMatch(specialty ->
                                        roomSpecialtyId.equals(
                                                specialty.getSpecialtyId()
                                        )
                                )
                )
                .toList();
    }



    public WorkScheduleResponse assignDoctor(
            UUID roomId,
            CreateWorkScheduleRequest request,
            String token
    ) {
        // 1. Kiểm tra dữ liệu đầu vào
        if (request.doctorId() == null
                || request.date() == null
                || request.startTime() == null
                || request.endTime() == null) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn bác sĩ, ngày và giờ làm việc"
            );
        }

        // 2. Lấy thông tin phòng
        RoomResponse room = roomClient.getRoomById(roomId, token);

        if (room.getSpecialtyId() == null) {
            throw new IllegalArgumentException(
                    "Phòng chưa được cấu hình chuyên khoa"
            );
        }

        // 3. Kiểm tra giờ làm việc
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException(
                    "Giờ kết thúc phải sau giờ bắt đầu"
            );
        }

        // 4. Lấy thông tin bác sĩ
        DoctorResponse doctor =
                doctorClient.getDoctorById(request.doctorId(), token);

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy bác sĩ"
            );
        }

        // 5. Kiểm tra chuyên khoa của bác sĩ có khớp với phòng không
        boolean hasMatchingSpecialty =
                doctor.getSpecialties() != null
                        && doctor.getSpecialties().stream()
                        .anyMatch(specialty ->
                                room.getSpecialtyId().equals(
                                        specialty.getSpecialtyId()
                                )
                        );

        if (!hasMatchingSpecialty) {
            throw new IllegalArgumentException(
                    "Bác sĩ không thuộc chuyên khoa của phòng"
            );
        }

        // 6. Tạo yêu cầu đăng ký lịch làm việc
        CreateWorkScheduleRequest scheduleRequest =
                new CreateWorkScheduleRequest(
                        request.doctorId(),
                        room.getClinicId(),
                        room.getSpecialtyId(),
                        roomId,
                        request.date(),
                        request.startTime(),
                        request.endTime()
                );

        // 7. Gửi yêu cầu sang scheduling-service
        return workScheduleClient.createWorkSchedule(
                scheduleRequest,
                token
        );
    }


    public List<WorkScheduleResponse> getRoomAssignments(
            UUID roomId,
            java.time.LocalDate date,
            String token
    ) {
        List<WorkScheduleResponse> schedules =
                workScheduleClient.getSchedulesByRoomAndDate(
                        roomId,
                        date,
                        token
                );

        if (schedules == null || schedules.isEmpty()) {
            return schedules;
        }

        for (WorkScheduleResponse schedule : schedules) {
            if (schedule.getDoctorId() == null) {
                continue;
            }

            try {
                DoctorResponse doctor = doctorClient.getDoctorById(
                        schedule.getDoctorId(),
                        token
                );

                if (doctor != null) {
                    schedule.setDoctorName(doctor.getFullName());
                }
            } catch (Exception e) {
                // Không để lỗi tra cứu một bác sĩ làm hỏng toàn bộ danh sách.
                schedule.setDoctorName(null);
            }
        }

        return schedules;
    }


}

