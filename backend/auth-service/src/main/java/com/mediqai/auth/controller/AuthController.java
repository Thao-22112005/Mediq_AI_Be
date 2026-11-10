package com.mediqai.auth.controller;

import com.mediqai.auth.dto.request.*;
import java.util.Map;
import com.mediqai.auth.dto.response.LoginResponse;
import com.mediqai.auth.dto.response.RegisterResponse;
import com.mediqai.auth.dto.response.UserResponse;
import com.mediqai.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ==============================
    // REGISTER
    // ==============================

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    // lay tkhoan theo access token
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {

        UserResponse response =
                authService.getCurrentUser(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-register")
    public ResponseEntity<String> verifyRegister(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        authService.verifyRegisterOtp(request);

        return ResponseEntity.ok(
                "Xác thực tài khoản thành công"
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                "Nếu email tồn tại, mã OTP đã được gửi"
        );
    }

    @PostMapping("/verify-forgot-password")
    public ResponseEntity<String> verifyForgotPassword(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        authService.verifyForgotPasswordOtp(request);

        return ResponseEntity.ok(
                "Xác thực OTP thành công"
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                "Đặt lại mật khẩu thành công"
        );
    }

    @PostMapping("/resend-register-otp")
    public ResponseEntity<String> resendRegisterOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {

        authService.resendRegisterOtp(request);

        return ResponseEntity.ok(
                "OTP mới đã được gửi đến email"
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,

            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,

            @RequestPart(value = "avatar", required = false)
            MultipartFile avatar
    ) throws Exception {

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName(fullName);
        request.setPhone(phone);

        UserResponse response =
                authService.updateProfile(
                        authentication.getName(),
                        request,
                        avatar
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/users/by-email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email
    ) {
        return ResponseEntity.ok(
                authService.getUserByEmail(email)
        );
    }


}