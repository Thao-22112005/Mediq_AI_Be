package com.mediqai.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtp(
            String toEmail,
            String otp,
            String purpose
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);

        if ("REGISTER".equals(purpose)) {
            message.setSubject(
                    "MEDIQ-AI - Xác thực tài khoản"
            );

            message.setText(
                    "Xin chào,\n\n"
                            + "Mã OTP xác thực tài khoản MEDIQ-AI của bạn là: "
                            + otp
                            + "\n\n"
                            + "Mã OTP có hiệu lực trong 2 phút."
                            + "\n"
                            + "Nếu bạn không thực hiện đăng ký, "
                            + "vui lòng bỏ qua email này."
                            + "\n\n"
                            + "MEDIQ-AI"
            );

        } else {

            message.setSubject(
                    "MEDIQ-AI - Đặt lại mật khẩu"
            );

            message.setText(
                    "Xin chào,\n\n"
                            + "Mã OTP đặt lại mật khẩu MEDIQ-AI của bạn là: "
                            + otp
                            + "\n\n"
                            + "Mã OTP có hiệu lực trong 2 phút."
                            + "\n"
                            + "Nếu bạn không yêu cầu đặt lại mật khẩu, "
                            + "vui lòng bỏ qua email này."
                            + "\n\n"
                            + "MEDIQ-AI"
            );
        }

        mailSender.send(message);
    }
}