
package com.mediqai.notification.dto;

public record NotificationMessage(
        String to,
        String subject,
        String body
) {
}
