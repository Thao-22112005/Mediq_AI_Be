package com.mediqai.ai.dto.response;

import com.mediqai.ai.entity.Specialty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {
    //AI đang cần hỏi thêm hay đã đủ thông tin
    private String status;

    private String reply;

    private String question;

    //Các triệu chứng AI đã xác định
    private List<String> symptoms;

    private String triageLevel;

    //Các chuyên khoa y tế
    private Specialty specialty;

    private String summary;

    private List<String> possibleConditions;
}