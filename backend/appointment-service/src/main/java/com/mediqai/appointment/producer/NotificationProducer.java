package com.mediqai.appointment.producer;

import com.mediqai.appointment.config.RabbitMQConfig;
import com.mediqai.appointment.dto.notification.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendNotification(NotificationMessage message) {

        try {
            String json = objectMapper.writeValueAsString(message);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    json
            );

            System.out.println(
                    "Notification published successfully to: "
                            + message.to()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to publish notification",
                    e
            );
        }
    }
}