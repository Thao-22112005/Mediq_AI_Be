package com.mediqai.ai.dto.response;

import com.mediqai.ai.entity.Message;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {

    private Long id;

    private Message.Sender sender;

    private String content;

    private LocalDateTime createdAt;
}