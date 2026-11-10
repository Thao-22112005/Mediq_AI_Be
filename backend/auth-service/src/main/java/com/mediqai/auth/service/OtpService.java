package com.mediqai.auth.service;

import com.mediqai.auth.entity.OtpType;
import com.mediqai.auth.entity.OtpVerification;
import com.mediqai.auth.entity.User;
import com.mediqai.auth.exception.BadRequestException;
import com.mediqai.auth.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRATION_MINUTES = 2;
    private static final int RESEND_COOLDOWN_SECONDS = 120;

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    // ==============================
    // CREATE + SEND OTP
    // ==============================

    public void createAndSendOtp(
            User user,
            OtpType type
    ) {

        LocalDateTime now = LocalDateTime.now();

        OtpVerification latestOtp =
                otpRepository
                        .findTopByUserAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
                                user,
                                type
                        )
                        .orElse(null);

        // ==============================
        // KIỂM TRA COOLDOWN
        // ==============================

        if (latestOtp != null) {

            long seconds =
                    Duration
                            .between(
                                    latestOtp.getCreatedAt(),
                                    now
                            )
                            .getSeconds();

            if (seconds < RESEND_COOLDOWN_SECONDS) {

                long remaining =
                        RESEND_COOLDOWN_SECONDS - seconds;

                throw new BadRequestException(
                        "Vui lòng chờ "
                                + remaining
                                + " giây trước khi gửi lại OTP"
                );
            }
        }

        // ==============================
        // VÔ HIỆU OTP CŨ
        // ==============================

        if (latestOtp != null) {
            otpRepository.delete(latestOtp);
        }

        // ==============================
        // TẠO OTP MỚI
        // ==============================

        String otp = generateOtp();

        OtpVerification otpVerification =
                OtpVerification.builder()
                        .user(user)
                        .otp(otp)
                        .type(type)
                        .expiresAt(
                                now.plusMinutes(
                                        OTP_EXPIRATION_MINUTES
                                )
                        )
                        .verified(false)
                        .build();

        otpRepository.save(otpVerification);

        // ==============================
        // GỬI EMAIL
        // ==============================

        emailService.sendOtp(
                user.getEmail(),
                otp,
                type.name()
        );
    }

    // ==============================
    // VERIFY OTP
    // ==============================

    public void verifyOtp(
            User user,
            String otp,
            OtpType type
    ) {

        OtpVerification verification =
                otpRepository
                        .findTopByUserAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
                                user,
                                type
                        )
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "OTP không hợp lệ"
                                )
                        );

        // ==============================
        // KIỂM TRA HẾT HẠN
        // ==============================

        if (verification.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "OTP đã hết hạn"
            );
        }

        // ==============================
        // KIỂM TRA OTP
        // ==============================

        if (!verification.getOtp().equals(otp)) {

            throw new BadRequestException(
                    "OTP không chính xác"
            );
        }

        // ==============================
        // XÁC THỰC THÀNH CÔNG
        // ==============================

        verification.setVerified(true);

        otpRepository.save(verification);
    }

    // ==============================
    // KIỂM TRA OTP ĐÃ VERIFY
    // ==============================

    public boolean isOtpVerified(User user, OtpType type) {
        return otpRepository
                .findTopByUserAndTypeAndVerifiedTrueOrderByCreatedAtDesc(user, type)
                .isPresent();
    }

    // ==============================
    // CONSUME OTP SAU KHI RESET
    // ==============================

    public void consumeVerifiedOtp(
            User user,
            OtpType type
    ) {

        otpRepository
                .findTopByUserAndTypeAndVerifiedTrueOrderByCreatedAtDesc(
                        user,
                        type
                )
                .ifPresent(otp -> {

                    otp.setVerified(false);

                    otpRepository.save(otp);
                });
    }

    // ==============================
    // GENERATE OTP
    // ==============================

    private String generateOtp() {

        SecureRandom random = new SecureRandom();

        return String.format(
                "%06d",
                random.nextInt(1_000_000)
        );
    }
}