package com.mediqai.auth.dto.request;

import com.mediqai.auth.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull(message = "Role không được để trống")
    private Role role;
}