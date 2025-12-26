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
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long tokenExpirationSeconds;
    private static final int MINIMUM_SECRET_LENGTH = 32; // 256 bits

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration-seconds:3600}") long tokenExpirationSeconds
    ) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MINIMUM_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                "JWT secret key must be at least " + MINIMUM_SECRET_LENGTH + " bytes (256 bits) long. " +
                "Current length: " + secretBytes.length + " bytes"
            );
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.tokenExpirationSeconds = tokenExpirationSeconds;
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

    /**
     * トークンを検証し、有効な場合はユーザーIDを返す（最適化版）
     * このメソッドは1回のパースで検証とユーザーID抽出の両方を行う
     *
     * @param token JWTトークン
     * @return 有効な場合はユーザーID、無効な場合はempty
     */
    public Optional<String> validateAndExtractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            // 有効期限チェック
            if (claims.getExpiration().after(Date.from(Instant.now()))) {
                return Optional.of(claims.getSubject());
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String generateToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(tokenExpirationSeconds);

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * カスタム有効期限でJWTトークンを生成（テスト用）
     *
     * @param userId ユーザーID
     * @param expirationSeconds 有効期限（秒）
     * @return JWTトークン
     */
    public String generateTokenWithCustomExpiration(String userId, long expirationSeconds) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
            .subject(userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }
}
