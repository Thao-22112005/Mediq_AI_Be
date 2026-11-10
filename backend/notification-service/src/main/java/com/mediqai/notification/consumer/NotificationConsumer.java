
package com.mediqai.notification.consumer;

import com.mediqai.notification.config.RabbitMQConfig;
import com.mediqai.notification.dto.NotificationMessage;
import com.mediqai.notification.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotificationConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(
            EmailService emailService,
            ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {

        try {
            NotificationMessage notification =
                    objectMapper.readValue(
                            message,
                            NotificationMessage.class
                    );

            emailService.sendEmail(
                    notification.to(),
                    notification.subject(),
                    notification.body()
            );

            System.out.println("Email sent successfully to: "
                    + notification.to());

        } catch (Exception e) {
            System.err.println("Failed to process notification: "
                    + e.getMessage());

            throw new RuntimeException(
                    "Notification processing failed", e
            );
        }
    }
}
