package com.mediqai.appointment.service;

import com.mediqai.appointment.client.AuthClient;
import com.mediqai.appointment.client.DoctorClient;
import com.mediqai.appointment.client.PatientClient;
import com.mediqai.appointment.client.SchedulingClient;
import com.mediqai.appointment.dto.response.*;
import com.mediqai.appointment.dto.request.CreateAppointmentRequest;
import com.mediqai.appointment.dto.request.RescheduleAppointmentRequest;
import com.mediqai.appointment.dto.request.UpdateAppointmentStatusRequest;
import com.mediqai.appointment.entity.Appointment;
import com.mediqai.appointment.entity.AppointmentHistory;
import com.mediqai.appointment.entity.AppointmentStatus;
import com.mediqai.appointment.event.AppointmentCreatedEvent;
import com.mediqai.appointment.repository.AppointmentHistoryRepository;
import com.mediqai.appointment.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mediqai.appointment.event.AppointmentNotificationEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentHistoryRepository appointmentHistoryRepository;
    private final SchedulingClient schedulingClient;
    private final AuthClient authClient;
    private final PatientClient patientClient;
    private final DoctorClient doctorClient;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * =========================================================
     * TẠO APPOINTMENT
     * =========================================================
     *
     * Patient đặt lịch bằng Slot.
     *
     * Patient được xác định thông qua JWT:
     *
     * JWT
     *   ↓
     * Patient Service /me
     *   ↓
     * Patient.id
     */
    @Transactional
    public AppointmentResponse createAppointment(
            CreateAppointmentRequest request,
            String token) {

        // 1. Lấy Patient hiện tại từ JWT
        PatientResponse patient =
                patientClient.getMyPatient(token);

        if (patient == null || patient.getId() == null) {
            throw new RuntimeException(
                    "Không tìm thấy hồ sơ bệnh nhân");
        }

        Long patientId = patient.getId();

        // 2. Lấy Slot
        SlotResponse slot =
                schedulingClient.getSlotById(
                        request.getSlotId());

        if (slot == null) {
            throw new RuntimeException(
                    "Không tìm thấy slot");
        }

        // 3. Slot phải AVAILABLE
        if (!"AVAILABLE".equalsIgnoreCase(
                slot.getStatus())) {

            throw new RuntimeException(
                    "Slot không còn khả dụng để đặt lịch");
        }

        // 4. Slot phải có WorkSchedule
        if (slot.getScheduleId() == null) {
            throw new RuntimeException(
                    "Slot chưa được gắn với WorkSchedule");
        }

        // 5. Lấy WorkSchedule
        WorkScheduleResponse schedule =
                schedulingClient.getWorkScheduleById(
                        slot.getScheduleId());

        if (schedule == null) {
            throw new RuntimeException(
                    "Không tìm thấy WorkSchedule");
        }

        // 6. WorkSchedule phải APPROVED
        if (schedule.getStatus() == null ||
                !"APPROVED".equalsIgnoreCase(
                        schedule.getStatus())) {

            throw new RuntimeException(
                    "WorkSchedule chưa được approve");
        }

        // 7. Kiểm tra Slot thuộc WorkSchedule
        if (!slot.getScheduleId()
                .equals(schedule.getScheduleId())) {

            throw new RuntimeException(
                    "Slot không thuộc WorkSchedule hợp lệ");
        }

        // 7.1. Bắt buộc đặt lịch trước ít nhất 1 ngày (không cho đặt cùng ngày hoặc quá khứ)
        LocalDate today = LocalDate.now();
        if (schedule.getDate() != null && !schedule.getDate().isAfter(today)) {
            throw new IllegalArgumentException(
                    "Quy định đặt khám: Quý khách phải đặt lịch trước ít nhất 1 ngày (từ ngày mai trở đi). Ngày khám đã chọn không còn khả dụng."
            );
        }

        // 8. Hold Slot
        SlotResponse heldSlot =
                schedulingClient.holdSlot(
                        request.getSlotId());

        if (heldSlot == null) {
            throw new RuntimeException(
                    "Không thể giữ slot");
        }

        try {

            // 9. Book Slot
            SlotResponse bookedSlot =
                    schedulingClient.bookSlot(
                            request.getSlotId());

            if (bookedSlot == null) {
                throw new RuntimeException(
                        "Không thể book slot");
            }

            // 10. Tạo Appointment
            Appointment appointment =
                    Appointment.builder()
                            .patientId(patientId)
                            .doctorId(schedule.getDoctorId())
                            .clinicId(schedule.getClinicId())
                            .specialtyId(schedule.getSpecialtyId())
                            .roomId(schedule.getRoomId())
                            .slotId(request.getSlotId())
                            .appointmentDate(schedule.getDate())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .status(AppointmentStatus.PENDING)
                            .reason(request.getReason())
                            .build();

            Appointment savedAppointment =
                    appointmentRepository.save(
                            appointment);

            // 11. Lưu history
            AppointmentHistory history =
                    AppointmentHistory.builder()
                            .appointmentId(
                                    savedAppointment
                                            .getAppointmentId())
                            .oldStatus(null)
                            .newStatus(
                                    AppointmentStatus.PENDING)
                            .changedBy(null)
                            .reason("Tạo lịch hẹn")
                            .build();

            appointmentHistoryRepository.save(history);

// 12. Phát sự kiện gửi email sau khi tạo lịch hẹn
            eventPublisher.publishEvent(
                    new AppointmentCreatedEvent(
                            patient.getUserId(),
                            patient.getFullName(),
                            savedAppointment.getAppointmentId().toString(),
                            savedAppointment.getAppointmentDate().toString(),
                            savedAppointment.getStartTime().toString(),
                            savedAppointment.getEndTime().toString()
                    )
            );

            return mapToResponse(savedAppointment);

        } catch (Exception e) {

            // Rollback Slot nếu tạo Appointment thất bại
            try {
                schedulingClient.releaseSlot(
                        request.getSlotId());
            } catch (Exception releaseException) {
                // Không che mất lỗi gốc
            }

            throw new RuntimeException(
                    "Không thể tạo lịch hẹn: "
                            + e.getMessage(),
                    e
            );
        }
    }

    /**
     * =========================================================
     * PATIENT OWNERSHIP
     * =========================================================
     *
     * Kiểm tra Appointment có thuộc Patient hiện tại hay không.
     */
    private Appointment getPatientOwnedAppointment(
            UUID appointmentId,
            String token) {

        // 1. Lấy Patient hiện tại
        PatientResponse patient =
                patientClient.getMyPatient(token);

        if (patient == null ||
                patient.getId() == null) {

            throw new RuntimeException(
                    "Không tìm thấy hồ sơ bệnh nhân");
        }

        // 2. Lấy Appointment
        Appointment appointment =
                appointmentRepository.findById(
                                appointmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy lịch hẹn"));

        // 3. Kiểm tra ownership
        if (!appointment.getPatientId()
                .equals(patient.getId())) {

            throw new RuntimeException(
                    "Bạn không có quyền truy cập lịch hẹn này");
        }

        return appointment;
    }

    /**
     * =========================================================
     * PATIENT - XEM CHI TIẾT APPOINTMENT CỦA MÌNH
     * =========================================================
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getPatientAppointmentById(
            UUID appointmentId,
            String token) {

        Appointment appointment =
                getPatientOwnedAppointment(
                        appointmentId,
                        token);

        return mapToResponse(appointment);
    }

    /**
     * =========================================================
     * PATIENT - XEM DANH SÁCH APPOINTMENT CỦA MÌNH
     * =========================================================
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(
            String token) {

        PatientResponse patient =
                patientClient.getMyPatient(token);

        if (patient == null ||
                patient.getId() == null) {

            throw new RuntimeException(
                    "Không tìm thấy hồ sơ bệnh nhân");
        }

        return appointmentRepository
                .findByPatientId(patient.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * =========================================================
     * PATIENT - HỦY APPOINTMENT CỦA MÌNH
     * =========================================================
     */
    @Transactional
    public AppointmentResponse cancelByPatient(
            UUID appointmentId,
            UpdateAppointmentStatusRequest request,
            String token) {

        // Kiểm tra ownership
        getPatientOwnedAppointment(
                appointmentId,
                token);

        // Patient chỉ được CANCELLED
        if (request.getStatus()
                != AppointmentStatus.CANCELLED) {

            throw new RuntimeException(
                    "Patient chỉ được hủy lịch hẹn");
        }

        return updateStatus(
                appointmentId,
                request);
    }

    /**
     * =========================================================
     * CẬP NHẬT STATUS
     * =========================================================
     */
    @Transactional
    public AppointmentResponse updateStatus(
            UUID appointmentId,
            UpdateAppointmentStatusRequest request) {

        // 1. Tìm Appointment
        Appointment appointment =
                appointmentRepository.findById(
                                appointmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy lịch hẹn"));

        AppointmentStatus oldStatus =
                appointment.getStatus();

        AppointmentStatus newStatus =
                request.getStatus();

        // 2. Không cho update cùng status
        if (oldStatus == newStatus) {
            throw new RuntimeException(
                    "Trạng thái mới phải khác trạng thái hiện tại");
        }

        // 3. Kiểm tra transition
        validateStatusTransition(
                oldStatus,
                newStatus);

        // 4. Update
        appointment.setStatus(newStatus);

        Appointment updatedAppointment =
                appointmentRepository.save(
                        appointment);

        // 5. Lưu history
        AppointmentHistory history =
                AppointmentHistory.builder()
                        .appointmentId(
                                appointment.getAppointmentId())
                        .oldStatus(oldStatus)
                        .newStatus(newStatus)
                        .changedBy(null)
                        .reason(request.getReason())
                        .build();

        appointmentHistoryRepository.save(history);

        // 6. Nếu CANCELLED
        if (newStatus == AppointmentStatus.CANCELLED) {

            try {
                schedulingClient.cancelBooking(
                        appointment.getSlotId());

            } catch (Exception e) {

                throw new RuntimeException(
                        "Appointment đã chuyển CANCELLED " +
                                "nhưng không thể cancel booking của slot",
                        e
                );
            }
        }

// Gửi thông báo sau khi cập nhật trạng thái thành công
        publishAppointmentNotification(
                updatedAppointment,
                "STATUS_CHANGED",
                null,
                null,
                null
        );

        return mapToResponse(updatedAppointment);
    }

    /**
     * =========================================================
     * KIỂM TRA STATUS TRANSITION
     * =========================================================
     */
    private void validateStatusTransition(
            AppointmentStatus oldStatus,
            AppointmentStatus newStatus) {

        // PENDING
        if (oldStatus ==
                AppointmentStatus.PENDING) {

            if (newStatus ==
                    AppointmentStatus.CONFIRMED
                    || newStatus ==
                    AppointmentStatus.CANCELLED) {

                return;
            }

            throw new RuntimeException(
                    "Không thể chuyển Appointment từ PENDING sang "
                            + newStatus);
        }

        // CONFIRMED
        if (oldStatus ==
                AppointmentStatus.CONFIRMED) {

            if (newStatus ==
                    AppointmentStatus.COMPLETED
                    || newStatus ==
                    AppointmentStatus.NO_SHOW
                    || newStatus ==
                    AppointmentStatus.CANCELLED) {

                return;
            }

            throw new RuntimeException(
                    "Không thể chuyển Appointment từ CONFIRMED sang "
                            + newStatus);
        }

        // COMPLETED
        if (oldStatus ==
                AppointmentStatus.COMPLETED) {

            throw new RuntimeException(
                    "Appointment đã COMPLETED, không thể thay đổi trạng thái");
        }

        // NO_SHOW
        if (oldStatus ==
                AppointmentStatus.NO_SHOW) {

            throw new RuntimeException(
                    "Appointment đã NO_SHOW, không thể thay đổi trạng thái");
        }

        // CANCELLED
        if (oldStatus ==
                AppointmentStatus.CANCELLED) {

            throw new RuntimeException(
                    "Appointment đã CANCELLED, không thể thay đổi trạng thái");
        }
    }

    /**
     * =========================================================
     * MAP ENTITY -> RESPONSE
     * =========================================================
*/


    private AppointmentResponse mapToResponse(Appointment appointment) {
        String doctorName = null;
        String specialtyName = null;
        String clinicName = null;
        String roomName = null;
        String roomCode = null;
        String patientName = null;

        if(appointment.getPatientId() != null){
            try{
                var patient = patientClient.getPatientById(appointment.getPatientId());
                if(patient != null){
                    patientName = patient.getFullName();
                }
            }catch(Exception e){

            }
        }
        // Lấy tên bác sĩ
        if (appointment.getDoctorId() != null) {
            try {
                var doctor = doctorClient.getDoctorById(appointment.getDoctorId());
                if (doctor != null) {
                    doctorName = doctor.getFullName();
                }
            } catch (Exception e) {
                // Không để lỗi lấy thông tin bác sĩ làm hỏng toàn bộ response
            }
        }

        // Lấy tên chuyên khoa
        if (appointment.getSpecialtyId() != null) {
            try {
                var specialty = schedulingClient.getSpecialtyById(
                        appointment.getSpecialtyId()
                );
                if (specialty != null) {
                    specialtyName = specialty.getName();
                }
            } catch (Exception e) {
                // Giữ specialtyName = null nếu không lấy được dữ liệu
            }
        }

        // Lấy tên phòng khám
        if (appointment.getClinicId() != null) {
            try {
                var clinic = schedulingClient.getClinicById(
                        appointment.getClinicId()
                );
                if (clinic != null) {
                    clinicName = clinic.getName();
                }
            } catch (Exception e) {
                // Giữ clinicName = null nếu không lấy được dữ liệu
            }
        }

        // Lấy tên và mã phòng
        if (appointment.getRoomId() != null) {
            try {
                var room = schedulingClient.getRoomById(
                        appointment.getRoomId()
                );
                if (room != null) {
                    roomName = room.getName();
                    roomCode = room.getCode();
                }
            } catch (Exception e) {
                // Giữ thông tin phòng = null nếu không lấy được dữ liệu
            }
        }

        return AppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .patientName(patientName)
                .doctorId(appointment.getDoctorId())
                .doctorName(doctorName)
                .clinicId(appointment.getClinicId())
                .clinicName(clinicName)
                .specialtyId(appointment.getSpecialtyId())
                .specialtyName(specialtyName)
                .roomId(appointment.getRoomId())
                .roomName(roomName)
                .roomCode(roomCode)
                .slotId(appointment.getSlotId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }




    /**
     * =========================================================
     * GET APPOINTMENT BY ID
     * =========================================================
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(
            UUID appointmentId) {

        Appointment appointment =
                appointmentRepository.findById(
                                appointmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy lịch hẹn"));

        return mapToResponse(appointment);
    }

    /**
     * =========================================================
     * GET APPOINTMENTS
     * =========================================================
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointments(
            Long patientId,
            UUID doctorId,
            UUID clinicId,
            LocalDate date,
            AppointmentStatus status) {

        List<Appointment> appointments;

        if (patientId != null) {

            appointments =
                    appointmentRepository
                            .findByPatientId(
                                    patientId);

        } else if (doctorId != null) {

            appointments =
                    appointmentRepository
                            .findByDoctorId(
                                    doctorId);

        } else if (clinicId != null) {

            appointments =
                    appointmentRepository
                            .findByClinicId(
                                    clinicId);

        } else if (date != null) {

            appointments =
                    appointmentRepository
                            .findByAppointmentDate(
                                    date);

        } else if (status != null) {

            appointments =
                    appointmentRepository
                            .findByStatus(
                                    status);

        } else {

            appointments =
                    appointmentRepository.findAll();
        }

        return appointments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * =========================================================
     * GET HISTORY
     * =========================================================
     */
    @Transactional(readOnly = true)
    public List<AppointmentHistoryResponse> getAppointmentHistory(
            UUID appointmentId) {

        appointmentRepository.findById(
                        appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy lịch hẹn"));

        return appointmentHistoryRepository
                .findByAppointmentIdOrderByCreatedAtAsc(
                        appointmentId)
                .stream()
                .map(history ->
                        AppointmentHistoryResponse
                                .builder()
                                .historyId(
                                        history.getHistoryId())
                                .appointmentId(
                                        history.getAppointmentId())
                                .oldStatus(
                                        history.getOldStatus())
                                .newStatus(
                                        history.getNewStatus())
                                .changedBy(
                                        history.getChangedBy())
                                .reason(
                                        history.getReason())
                                .oldSlotId(
                                        history.getOldSlotId())
                                .newSlotId(
                                        history.getNewSlotId())
                                .oldStartTime(
                                        history.getOldStartTime())
                                .newStartTime(
                                        history.getNewStartTime())
                                .createdAt(
                                        history.getCreatedAt())
                                .build()
                )
                .toList();
    }

    /**
     * =========================================================
     * PATIENT - ĐỔI LỊCH CỦA MÌNH
     * =========================================================
     */
    @Transactional
    public AppointmentResponse rescheduleByPatient(
            UUID appointmentId,
            RescheduleAppointmentRequest request,
            String token) {

        // Kiểm tra ownership trước
        getPatientOwnedAppointment(
                appointmentId,
                token);

        return rescheduleAppointment(
                appointmentId,
                request);
    }

    /**
     * =========================================================
     * RESCHEDULE APPOINTMENT
     * =========================================================
     */
    @Transactional
    public AppointmentResponse rescheduleAppointment(
            UUID appointmentId,
            RescheduleAppointmentRequest request) {

        // 1. Tìm Appointment
        Appointment appointment =
                appointmentRepository.findById(
                                appointmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy lịch hẹn"));

        // 2. Chỉ PENDING / CONFIRMED
        if (appointment.getStatus() !=
                AppointmentStatus.PENDING
                &&
                appointment.getStatus() !=
                        AppointmentStatus.CONFIRMED) {

            throw new RuntimeException(
                    "Chỉ có Appointment PENDING hoặc CONFIRMED "
                            + "mới được đổi lịch");
        }

        // 3. Slot mới phải khác Slot hiện tại
        if (appointment.getSlotId()
                .equals(request.getNewSlotId())) {

            throw new RuntimeException(
                    "Slot mới phải khác Slot hiện tại");
        }

        // Lưu slot cũ
        UUID oldSlotId = appointment.getSlotId();

        LocalDate oldAppointmentDate =
                appointment.getAppointmentDate();

        LocalTime oldStartTime =
                appointment.getStartTime();

        LocalTime oldEndTime =
                appointment.getEndTime();

        // 4. Lấy Slot mới
        SlotResponse newSlot =
                schedulingClient.getSlotById(
                        request.getNewSlotId());

        if (newSlot == null) {
            throw new RuntimeException(
                    "Không tìm thấy slot mới");
        }

        // 5. Slot mới phải AVAILABLE
        if (!"AVAILABLE".equalsIgnoreCase(
                newSlot.getStatus())) {

            throw new RuntimeException(
                    "Slot mới không còn AVAILABLE");
        }

        // 6. Slot mới phải có Schedule
        if (newSlot.getScheduleId() == null) {

            throw new RuntimeException(
                    "Slot mới chưa được gắn với WorkSchedule");
        }

        // 7. Lấy WorkSchedule mới
        WorkScheduleResponse newSchedule =
                schedulingClient.getWorkScheduleById(
                        newSlot.getScheduleId());

        if (newSchedule == null) {

            throw new RuntimeException(
                    "Không tìm thấy WorkSchedule của slot mới");
        }

        // 8. WorkSchedule mới phải APPROVED
        if (newSchedule.getStatus() == null ||
                !"APPROVED".equalsIgnoreCase(
                        newSchedule.getStatus())) {

            throw new RuntimeException(
                    "WorkSchedule của slot mới chưa được approve");
        }

        // 8.1. Bắt buộc dời lịch trước ít nhất 1 ngày
        LocalDate today = LocalDate.now();
        if (newSchedule.getDate() != null && !newSchedule.getDate().isAfter(today)) {
            throw new IllegalArgumentException(
                    "Quy định đặt khám: Quý khách phải chọn ngày khám mới trước ít nhất 1 ngày (từ ngày mai trở đi)."
            );
        }

        // 9. Hold Slot mới
        SlotResponse heldSlot =
                schedulingClient.holdSlot(
                        request.getNewSlotId());

        if (heldSlot == null) {

            throw new RuntimeException(
                    "Không thể giữ slot mới");
        }

        try {

            // 10. Book Slot mới
            SlotResponse bookedSlot =
                    schedulingClient.bookSlot(
                            request.getNewSlotId());

            if (bookedSlot == null) {

                throw new RuntimeException(
                        "Không thể book slot mới");
            }

            // 11. Update Appointment
            appointment.setSlotId(
                    request.getNewSlotId());

            appointment.setDoctorId(
                    newSchedule.getDoctorId());

            appointment.setClinicId(
                    newSchedule.getClinicId());

            appointment.setSpecialtyId(
                    newSchedule.getSpecialtyId());

            appointment.setRoomId(
                    newSchedule.getRoomId());

            appointment.setAppointmentDate(
                    newSchedule.getDate());

            appointment.setStartTime(
                    newSlot.getStartTime());

            appointment.setEndTime(
                    newSlot.getEndTime());

            if (request.getReason() != null &&
                    !request.getReason().isBlank()) {

                appointment.setReason(
                        request.getReason());
            }

            Appointment updatedAppointment =
                    appointmentRepository.save(
                            appointment);

            // 12. Lưu history reschedule
            AppointmentHistory history =
                    AppointmentHistory.builder()
                            .appointmentId(
                                    updatedAppointment
                                            .getAppointmentId())
                            .oldStatus(
                                    updatedAppointment
                                            .getStatus())
                            .newStatus(
                                    updatedAppointment
                                            .getStatus())
                            .changedBy(null)
                            .reason(
                                    request.getReason() != null &&
                                            !request.getReason()
                                                    .isBlank()
                                            ? request.getReason()
                                            : "Đổi lịch hẹn")
                            .oldSlotId(oldSlotId)
                            .newSlotId(
                                    request.getNewSlotId())
                            .oldStartTime(
                                    oldStartTime)
                            .newStartTime(
                                    newSlot.getStartTime())
                            .build();

            appointmentHistoryRepository.save(
                    history);

            // 13. Giải phóng Slot cũ
            try {

                schedulingClient.cancelBooking(
                        oldSlotId);

            } catch (Exception e) {

                throw new RuntimeException(
                        "Đã book slot mới nhưng không thể "
                                + "giải phóng slot cũ",
                        e);
            }

            // 14. Gửi thông báo đổi lịch
            publishAppointmentNotification(
                    updatedAppointment,
                    "RESCHEDULED",
                    oldAppointmentDate,
                    oldStartTime,
                    oldEndTime
            );

            return mapToResponse(updatedAppointment);

        } catch (Exception e) {

            // Nếu quá trình đổi lịch thất bại
            // thì giải phóng Slot mới
            try {

                schedulingClient.cancelBooking(
                        request.getNewSlotId());

            } catch (Exception ignored) {
                // Không che mất lỗi gốc
            }

            throw new RuntimeException(
                    "Không thể đổi lịch hẹn: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Transactional(readOnly = true)
    public List<SpecialtyAppointmentStatsResponse> getSpecialtyAppointmentStats(
            LocalDate monthStart,
            LocalDate nextMonthStart
    ) {
        return appointmentRepository.countCompletedAppointmentsBySpecialty(
                        monthStart,
                        nextMonthStart,
                        AppointmentStatus.COMPLETED
                )
                .stream()
                .map(item -> new SpecialtyAppointmentStatsResponse(
                        item.getSpecialtyId(),
                        item.getPatientsThisMonth()
                ))
                .toList();
    }

    private void publishAppointmentNotification(
            Appointment appointment,
            String eventType,
            LocalDate oldAppointmentDate,
            LocalTime oldStartTime,
            LocalTime oldEndTime) {

        eventPublisher.publishEvent(
                new AppointmentNotificationEvent(
                        appointment.getPatientId(),
                        appointment.getAppointmentId().toString(),
                        eventType,
                        appointment.getStatus().name(),
                        appointment.getAppointmentDate() != null
                                ? appointment.getAppointmentDate().toString()
                                : null,
                        appointment.getStartTime() != null
                                ? appointment.getStartTime().toString()
                                : null,
                        appointment.getEndTime() != null
                                ? appointment.getEndTime().toString()
                                : null,
                        oldAppointmentDate != null
                                ? oldAppointmentDate.toString()
                                : null,
                        oldStartTime != null
                                ? oldStartTime.toString()
                                : null,
                        oldEndTime != null
                                ? oldEndTime.toString()
                                : null
                )
        );
    }
}