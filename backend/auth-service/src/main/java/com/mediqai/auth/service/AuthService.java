package com.mediqai.auth.service;

import com.mediqai.auth.client.MediaClient;
import com.mediqai.auth.client.PatientClient;
import com.mediqai.auth.dto.request.*;
import com.mediqai.auth.dto.response.LoginResponse;
import com.mediqai.auth.dto.response.RegisterResponse;
import com.mediqai.auth.dto.response.UserResponse;
import com.mediqai.auth.entity.OtpType;
import com.mediqai.auth.entity.Role;
import com.mediqai.auth.entity.User;
import com.mediqai.auth.exception.BadRequestException;
import com.mediqai.auth.exception.DuplicateResourceException;
import com.mediqai.auth.repository.UserRepository;
import com.mediqai.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final MediaClient mediaClient;
    private final PatientClient patientClient;

    // ==============================
    // REGISTER
    // ==============================

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email đã được sử dụng"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException(
                    "Số điện thoại đã được sử dụng"
            );
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(Role.PATIENT)
                .status("PENDING")
                .build();

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.PATIENT) {
            patientClient.createPatient(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getPhone()
            );
        }

        otpService.createAndSendOtp(
                savedUser,
                OtpType.REGISTER
        );

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus())
                .build();
    }

    // ==============================
    // LOGIN
    // ==============================

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Email hoặc mật khẩu không đúng"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new BadRequestException(
                    "Email hoặc mật khẩu không đúng"
            );
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException(
                    "Tài khoản đã bị khóa hoặc không hoạt động"
            );
        }

        String accessToken = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus())
                .accessToken(accessToken)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // ==============================
    // GET CURRENT USER
    // ==============================

    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Không tìm thấy tài khoản"
                        )
                );

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus())
                .build();
    }

    // ==============================
    // VERIFY REGISTER OTP
    // ==============================

    public void verifyRegisterOtp(
            VerifyOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Tài khoản không tồn tại"
                        )
                );

        if ("ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException(
                    "Tài khoản đã được xác thực"
            );
        }

        otpService.verifyOtp(
                user,
                request.getOtp(),
                OtpType.REGISTER
        );

        user.setStatus("ACTIVE");

        userRepository.save(user);
    }

    // ==============================
    // FORGOT PASSWORD
    // ==============================

    public void forgotPassword(
            ForgotPasswordRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        // Không tiết lộ email có tồn tại hay không
        if (user == null) {
            return;
        }

        otpService.createAndSendOtp(
                user,
                OtpType.FORGOT_PASSWORD
        );
    }

    // ==============================
    // VERIFY FORGOT PASSWORD OTP
    // ==============================

    public void verifyForgotPasswordOtp(
            VerifyOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "OTP không hợp lệ"
                        )
                );

        otpService.verifyOtp(
                user,
                request.getOtp(),
                OtpType.FORGOT_PASSWORD
        );
    }

    // ==============================
    // RESET PASSWORD
    // ==============================

    public void resetPassword(
            ResetPasswordRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Không tìm thấy tài khoản"
                        )
                );

        // Bắt buộc OTP FORGOT_PASSWORD
        // phải được xác thực trước khi đổi password
        if (!otpService.isOtpVerified(
                user,
                OtpType.FORGOT_PASSWORD
        )) {
            throw new BadRequestException(
                    "Bạn chưa xác thực OTP hoặc OTP đã hết hiệu lực"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        // Không cho dùng lại OTP sau khi reset password
        otpService.consumeVerifiedOtp(
                user,
                OtpType.FORGOT_PASSWORD
        );
    }

    public void resendRegisterOtp(
            ResendOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "Tài khoản không tồn tại"
                        )
                );

        if ("ACTIVE".equals(user.getStatus())) {
            throw new BadRequestException(
                    "Tài khoản đã được xác thực"
            );
        }

        otpService.createAndSendOtp(
                user,
                OtpType.REGISTER
        );
    }

    public void changePassword(String email, ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash())) {

            throw new BadRequestException(
                    "Mật khẩu hiện tại không chính xác");
        }

        if (request.getCurrentPassword()
                .equals(request.getNewPassword())) {

            throw new BadRequestException(
                    "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

    public UserResponse updateProfile(
            String currentEmail,
            UpdateProfileRequest request,
            MultipartFile avatar
    ) throws IOException {

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() ->
                        new BadRequestException("Không tìm thấy tài khoản"));

        // Kiểm tra số điện thoại trùng
        if (!user.getPhone().equals(request.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {

            throw new DuplicateResourceException(
                    "Số điện thoại đã được sử dụng"
            );
        }

        // Cập nhật thông tin cơ bản
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        // ==========================================
        // REPLACE AVATAR
        // ==========================================

        if (avatar != null && !avatar.isEmpty()) {

            Map result = mediaClient.replaceAvatar(
                    avatar,
                    user.getRole().name(),
                    user.getId()
            );

            String newAvatarUrl =
                    (String) result.get("url");

            if (newAvatarUrl != null && !newAvatarUrl.isBlank()) {
                user.setAvatarUrl(newAvatarUrl);
            }
        }

        // ==========================================
        // LƯU USER
        // ==========================================

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus())
                .avatarUrl(savedUser.getAvatarUrl())
                .build();
    }

    public UserResponse getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Không tìm thấy tài khoản"
                        ));

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public UserResponse updateUserRole(
            Long id,
            String role
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Không tìm thấy tài khoản"
                        )
                );

        Role oldRole = user.getRole();

        Role newRole;

        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Role không hợp lệ: " + role
            );
        }

        // Không thay đổi gì nếu role giống nhau
        if (oldRole == newRole) {
            return UserResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole().name())
                    .status(user.getStatus())
                    .avatarUrl(user.getAvatarUrl())
                    .build();
        }

        // ==========================================
        // PATIENT → ROLE KHÁC
        // ==========================================

        if (oldRole == Role.PATIENT
                && newRole != Role.PATIENT) {

            patientClient.deletePatient(
                    user.getId()
            );
        }

        // ==========================================
        // ROLE KHÁC → PATIENT
        // ==========================================

        if (oldRole != Role.PATIENT
                && newRole == Role.PATIENT) {

            patientClient.createPatient(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone()
            );
        }

        // ==========================================
        // CẬP NHẬT ROLE
        // ==========================================

        user.setRole(newRole);

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus())
                .avatarUrl(savedUser.getAvatarUrl())
                .build();
    }

    public UserResponse deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Không tìm thấy tài khoản"
                        )
                );

        user.setStatus("LOCKED");

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus())
                .avatarUrl(savedUser.getAvatarUrl())
                .build();
    }

    public UserResponse createUserByAdmin(
            String fullName,
            String email,
            String phone,
            String password,
            String role
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException(
                    "Email đã được sử dụng"
            );
        }

        Role newRole;

        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Role không hợp lệ: " + role
            );
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(newRole)
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.PATIENT) {
            patientClient.createPatient(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getPhone()
            );
        }

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus())
                .avatarUrl(savedUser.getAvatarUrl())
                .build();
    }
}