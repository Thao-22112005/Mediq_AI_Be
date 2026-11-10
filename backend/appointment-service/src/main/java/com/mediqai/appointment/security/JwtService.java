package com.mediqai.appointment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    // ==============================
    // Tạo SecretKey
    // ==============================

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ==============================
    // Validate JWT
    // ==============================

    public boolean isTokenValid(String token) {

        try {

            getClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // ==============================
    // Extract email
    // ==============================

    public String extractEmail(String token) {

        return getClaims(token)
                .getSubject();
    }

    // ==============================
    // Extract role
    // ==============================

    public String extractRole(String token) {

        return getClaims(token)
                .get("role", String.class);
    }

    // ==============================
    // Get claims
    // ==============================

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}