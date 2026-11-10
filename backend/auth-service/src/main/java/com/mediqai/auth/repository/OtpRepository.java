package com.mediqai.auth.repository;

import com.mediqai.auth.entity.OtpType;
import com.mediqai.auth.entity.OtpVerification;
import com.mediqai.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository
        extends JpaRepository<OtpVerification, Long> {

    // OTP chưa xác thực - dùng khi verify OTP
    Optional<OtpVerification>
    findTopByUserAndTypeAndVerifiedFalseOrderByCreatedAtDesc(
            User user,
            OtpType type
    );

    // OTP đã xác thực - dùng để cho phép reset password
    Optional<OtpVerification>
    findTopByUserAndTypeAndVerifiedTrueOrderByCreatedAtDesc(
            User user,
            OtpType type
    );
}