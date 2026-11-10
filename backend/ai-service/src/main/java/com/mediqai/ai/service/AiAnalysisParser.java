package com.mediqai.ai.service;

import com.mediqai.ai.dto.response.AiAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class AiAnalysisParser {

    private final tools.jackson.databind.ObjectMapper objectMapper;

    public AiAnalysisResponse parse(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    AiAnalysisResponse.class
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể đọc kết quả phân tích từ AI",
                    e
            );
        }
    }
}