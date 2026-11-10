
package com.mediqai.appointment.listener;

import com.mediqai.appointment.client.AuthClient;
import com.mediqai.appointment.client.PatientClient;
import com.mediqai.appointment.dto.notification.NotificationMessage;
import com.mediqai.appointment.dto.response.PatientResponse;
import com.mediqai.appointment.dto.response.UserResponse;
import com.mediqai.appointment.event.AppointmentNotificationEvent;
import com.mediqai.appointment.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentNotificationListener {

    private final PatientClient patientClient;
    private final AuthClient authClient;
    private final NotificationProducer notificationProducer;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleAppointmentNotification(
            AppointmentNotificationEvent event) {

        try {
            // 1. Lấy hồ sơ bệnh nhân
            PatientResponse patient =
                    patientClient.getPatientById(event.patientId());

            if (patient == null || patient.getUserId() == null) {
                log.error(
                        "Không tìm thấy Patient/User ID cho appointment {}",
                        event.appointmentId()
                );
                return;
            }

            // 2. Lấy email từ Auth Service
            UserResponse user =
                    authClient.getUserById(patient.getUserId());

            if (user == null || user.getEmail() == null
                    || user.getEmail().isBlank()) {
                log.error(
                        "Không tìm thấy email bệnh nhân cho appointment {}",
                        event.appointmentId()
                );
                return;
            }

            // 3. Tạo nội dung email
            String subject = buildSubject(event);
            String body = buildBody(event, patient);

            // 4. Gửi qua RabbitMQ
            notificationProducer.sendNotification(
                    new NotificationMessage(
                            user.getEmail(),
                            subject,
                            body
                    )
            );

            log.info(
                    "Đã gửi yêu cầu thông báo {} cho appointment {} tới {}",
                    event.eventType(),
                    event.appointmentId(),
                    user.getEmail()
            );

        } catch (Exception e) {
            // Không làm ảnh hưởng transaction nghiệp vụ đã commit
            log.error(
                    "Không thể gửi thông báo cho appointment {}",
                    event.appointmentId(),
                    e
            );
        }
    }

    private String buildSubject(
            AppointmentNotificationEvent event) {

        return switch (event.eventType()) {
            case "CREATED" ->
                    "[MEDIQ-AI] Tiếp nhận lịch hẹn thành công";

            case "STATUS_CHANGED" ->
                    switch (event.status()) {
                        case "CONFIRMED" ->
                                "[MEDIQ-AI] Lịch khám đã được xác nhận";

                        case "CANCELLED" ->
                                "[MEDIQ-AI] Lịch khám đã bị hủy";

                        case "COMPLETED" ->
                                "[MEDIQ-AI] Lịch khám đã hoàn thành";

                        case "NO_SHOW" ->
                                "[MEDIQ-AI] Thông báo trạng thái lịch khám";

                        default ->
                                "[MEDIQ-AI] Cập nhật lịch khám";
                    };

            case "RESCHEDULED" ->
                    "[MEDIQ-AI] Đổi lịch khám thành công";

            default ->
                    "[MEDIQ-AI] Thông báo lịch khám";
        };
    }

    private String buildBody(
            AppointmentNotificationEvent event,
            PatientResponse patient) {

        StringBuilder body = new StringBuilder();

        body.append("Kính chào ")
                .append(patient.getFullName())
                .append(",\n\n");

        switch (event.eventType()) {
            case "CREATED" ->
                    body.append(
                            "MEDIQ-AI đã tiếp nhận yêu cầu đặt lịch khám của bạn."
                    );

            case "STATUS_CHANGED" ->
                    body.append(
                            switch (event.status()) {
                                case "CONFIRMED" ->
                                        "Lịch khám của bạn đã được bác sĩ xác nhận.";

                                case "CANCELLED" ->
                                        "Lịch khám của bạn đã được hủy.";

                                case "COMPLETED" ->
                                        "Lịch khám của bạn đã được ghi nhận hoàn thành.";

                                case "NO_SHOW" ->
                                        "Lịch khám của bạn được ghi nhận ở trạng thái không đến khám.";

                                default ->
                                        "Trạng thái lịch khám của bạn vừa được cập nhật.";
                            }
                    );

            case "RESCHEDULED" ->
                    body.append(
                            "Lịch khám của bạn đã được đổi thành công."
                    );

            default ->
                    body.append(
                            "Lịch khám của bạn vừa có cập nhật mới."
                    );
        }

        body.append("\n\n")
                .append("Mã lịch hẹn: ")
                .append(event.appointmentId())
                .append("\n");

        if ("RESCHEDULED".equals(event.eventType())) {
            body.append("\n--- LỊCH CŨ ---\n")
                    .append("Ngày khám: ")
                    .append(event.oldAppointmentDate())
                    .append("\nGiờ khám: ")
                    .append(event.oldStartTime())
                    .append(" - ")
                    .append(event.oldEndTime())
                    .append("\n\n--- LỊCH MỚI ---\n");
        }

        body.append("Ngày khám: ")
                .append(event.appointmentDate())
                .append("\nGiờ khám: ")
                .append(event.startTime())
                .append(" - ")
                .append(event.endTime())
                .append("\nTrạng thái: ")
                .append(event.status())
                .append("\n\n");

        body.append(
                "Vui lòng đăng nhập MEDIQ-AI để xem thông tin chi tiết lịch khám.\n\n"
        );

        body.append(
                "Trân trọng,\nĐội ngũ MEDIQ-AI"
        );

        return body.toString();
    }
}
