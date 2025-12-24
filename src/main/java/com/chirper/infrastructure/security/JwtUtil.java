package com.chirper.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.getExpiration().after(Date.from(Instant.now()));
        } catch (Exception e) {
            return false;
        }
    }

    public String generateToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(3600); // 1時間

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
    }
}
