package com.mediqai.media.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MediaResponse {

    private Long id;

    private String fileName;

    private String url;

    private String publicId;

    private String fileType;

    private String entityType;

    private Long entityId;

    private LocalDateTime createdAt;
}