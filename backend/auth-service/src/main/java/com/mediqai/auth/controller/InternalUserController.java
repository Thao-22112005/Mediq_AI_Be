package com.mediqai.auth.controller;

import com.mediqai.auth.dto.request.CreateUserByAdminRequest;
import com.mediqai.auth.dto.request.UpdateUserRoleRequest;
import com.mediqai.auth.dto.request.UpdateUserStatusRequest;
import com.mediqai.auth.dto.response.InternalUserResponse;
import com.mediqai.auth.entity.User;
import com.mediqai.auth.repository.UserRepository;
import com.mediqai.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    // ==============================
    // GET ALL USERS
    // ==============================

    @GetMapping
    public List<InternalUserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ==============================
    // GET USER BY ID
    // ==============================

    @GetMapping("/{id}")
    public InternalUserResponse getUserById(
            @PathVariable Long id
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy user với id: " + id
                        )
                );

        return toResponse(user);
    }

    // ==============================
    // UPDATE STATUS
    // ==============================

    @PutMapping("/{id}/status")
    public InternalUserResponse updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy user với id: " + id
                        )
                );

        String status = request.getStatus().toUpperCase();

        if (!status.equals("ACTIVE")
                && !status.equals("LOCKED")) {

            throw new IllegalArgumentException(
                    "Status chỉ được là ACTIVE hoặc LOCKED"
            );
        }

        user.setStatus(status);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    // ==============================
    // UPDATE ROLE
    // ==============================

    @PutMapping("/{id}/role")
    public InternalUserResponse updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {

        return toInternalUserResponse(
                authService.updateUserRole(
                        id,
                        request.getRole().name()
                )
        );
    }

    // ==============================
    // CONVERT RESPONSE
    // ==============================

    private InternalUserResponse toInternalUserResponse(
            com.mediqai.auth.dto.response.UserResponse response
    ) {

        return InternalUserResponse.builder()
                .id(response.getId())
                .fullName(response.getFullName())
                .email(response.getEmail())
                .phone(response.getPhone())
                .avatarUrl(response.getAvatarUrl())
                .role(
                        com.mediqai.auth.entity.Role.valueOf(
                                response.getRole()
                        )
                )
                .status(response.getStatus())
                .build();
    }

    private InternalUserResponse toResponse(User user) {

        return InternalUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @DeleteMapping("/{id}")
    public InternalUserResponse deleteUser(
            @PathVariable Long id
    ) {
        return toInternalUserResponse(
                authService.deleteUser(id)
        );
    }

    @PostMapping
    public InternalUserResponse createUser(
            @RequestBody CreateUserByAdminRequest request
    ) {
        return toInternalUserResponse(
                authService.createUserByAdmin(
                        request.getFullName(),
                        request.getEmail(),
                        request.getPhone(),
                        request.getPassword(),
                        request.getRole()
                )
        );
    }
}