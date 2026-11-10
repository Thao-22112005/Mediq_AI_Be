package com.mediqai.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // Tắt CSRF vì sử dụng REST API + JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Không sử dụng session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Phân quyền API
                .authorizeHttpRequests(auth -> auth

                        // Không cần JWT
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/verify-register",
                                "/api/auth/resend-register-otp",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/internal/users",
                                "/api/auth/internal/users/**"
                        ).permitAll()

                        // Các API khác bắt buộc đăng nhập
                        .anyRequest().authenticated()
                )

                // JWT Filter chạy trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}