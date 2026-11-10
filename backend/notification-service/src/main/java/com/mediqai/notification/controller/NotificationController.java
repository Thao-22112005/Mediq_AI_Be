
package com.mediqai.notification.controller;

import com.mediqai.notification.config.RabbitMQConfig;
import com.mediqai.notification.dto.NotificationMessage;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @RequestBody NotificationMessage notification) {

        try {
            String json = objectMapper.writeValueAsString(notification);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    "notification.email",
                    json
            );

            return ResponseEntity.ok(
                    "Notification published successfully!"
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to publish notification: "
                            + e.getMessage());
        }
    }
}
