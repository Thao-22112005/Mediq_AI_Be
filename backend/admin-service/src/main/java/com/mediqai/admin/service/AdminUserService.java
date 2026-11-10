package com.mediqai.admin.service;

import com.mediqai.admin.client.AuthClient;
import com.mediqai.admin.client.DoctorClient;
import com.mediqai.admin.dto.request.CreateDoctorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AuthClient authClient;
    private final DoctorClient doctorClient;

    public List<Map<String, Object>> getAllUsers(String token) {
        return authClient.getAllUsers(token);
    }

    public Map<String, Object> getUserById(String token, Long id) {
        return authClient.getUserById(token, id);
    }

    public Map<String, Object> updateUserStatus(
            String token, Long id, String status) {
        return authClient.updateUserStatus(token, id, status);
    }

    public Map<String, Object> updateUserRole(
            String token, Long id, String role) {
        return authClient.updateUserRole(token, id, role);
    }

    public Map<String, Object> deleteUser(String token, Long id) {
        return authClient.deleteUser(token, id);
    }

    public Map<String, Object> createUser(
            String token,
            Map<String, String> body) {

        // 1. Tạo tài khoản trong auth-service
        Map<String, Object> user = authClient.createUser(token, body);

        // 2. Chỉ tạo hồ sơ bác sĩ nếu role là DOCTOR
        String role = body.get("role");

        if (role == null || !"DOCTOR".equalsIgnoreCase(role.trim())) {
            return user;
        }

        // 3. Lấy ID tài khoản vừa tạo trong auth-service
        Object idValue = user.get("id");

        if (!(idValue instanceof Number)) {
            throw new IllegalStateException(
                    "Không lấy được ID dạng số từ kết quả tạo user");
        }

        Long userId = ((Number) idValue).longValue();

        // 4. Lấy thông tin tài khoản
        String fullName = (String) user.get("fullName");
        String phoneNumber = (String) user.get("phone");

        if (fullName == null || fullName.isBlank()
                || phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalStateException(
                    "Thiếu họ tên hoặc số điện thoại trong tài khoản vừa tạo");
        }

        // 5. Tạo hồ sơ bác sĩ, chưa cần licenseNumber hoặc specialties
        CreateDoctorRequest doctorRequest = CreateDoctorRequest.builder()
                .userId(userId)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .licenseNumber(null)
                .specialties(null)
                .build();

        doctorClient.createDoctor(doctorRequest, token);

        return user;
    }

}