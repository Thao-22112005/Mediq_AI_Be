package com.mediqai.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String accessToken;
    private String avatarUrl;
}