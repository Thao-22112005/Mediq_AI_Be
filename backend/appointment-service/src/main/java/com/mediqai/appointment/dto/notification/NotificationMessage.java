package com.mediqai.appointment.dto.notification;

public record NotificationMessage(
        String to,
        String subject,
        String body
) {
}