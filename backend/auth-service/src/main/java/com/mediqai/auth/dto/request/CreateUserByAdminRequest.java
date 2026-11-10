package com.mediqai.auth.dto.request;

import lombok.Data;

@Data
public class CreateUserByAdminRequest {

    private String fullName;

    private String email;

    private String phone;

    private String password;

    private String role;
}