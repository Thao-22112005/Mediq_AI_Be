package com.mediqai.admin.controller;

import com.mediqai.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @org.springframework.web.bind.annotation.RequestHeader(
                    HttpHeaders.AUTHORIZATION
            ) String authorizationHeader
    ) {

        String token = authorizationHeader.substring(7);

        List<Map<String, Object>> users =
                adminUserService.getAllUsers(token);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long id
    ) {

        String token = authorizationHeader.substring(7);

        Map<String, Object> user =
                adminUserService.getUserById(token, id);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {

        String token = authorizationHeader.substring(7);

        String status = request.get("status");

        Map<String, Object> user =
                adminUserService.updateUserStatus(
                        token,
                        id,
                        status
                );

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {

        String token = authorizationHeader.substring(7);

        String role = request.get("role");

        Map<String, Object> user =
                adminUserService.updateUserRole(
                        token,
                        id,
                        role
                );

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable Long id
    ) {
        String token = authorizationHeader.substring(7);

        Map<String, Object> user =
                adminUserService.deleteUser(token, id);

        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody Map<String, String> request
    ) {
        String token = authorizationHeader.substring(7);

        Map<String, Object> user =
                adminUserService.createUser(token, request);

        return ResponseEntity.ok(user);
    }

}