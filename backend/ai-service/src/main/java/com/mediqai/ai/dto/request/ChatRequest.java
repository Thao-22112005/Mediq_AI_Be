package com.mediqai.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    private Long conversationId;

    @NotBlank(message = "Tin nhắn không được để trống")
    private String message;
}