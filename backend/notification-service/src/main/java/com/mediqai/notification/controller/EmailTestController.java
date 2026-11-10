
package com.mediqai.notification.controller;

import com.mediqai.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/test")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/email")
    public ResponseEntity<String> sendTestEmail(
            @RequestParam String to) {

        emailService.sendEmail(
                to,
                "MEDIQ-AI - Test Email",
                "Xin chao! Gmail SMTP cua MEDIQ-AI da hoat dong thanh cong."
        );

        return ResponseEntity.ok("Email sent successfully!");
    }
}
