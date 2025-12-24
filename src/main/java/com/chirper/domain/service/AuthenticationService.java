package com.chirper.domain.service;

import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * AuthenticationService
 * 認証に関するドメインサービス
 * パスワード検証とJWT生成・検証を担当
 */
public class AuthenticationService {

    private final SecretKey jwtSecretKey;
    private static final long JWT_EXPIRATION_HOURS = 1;

    /**
     * コンストラクタ
     * @param jwtSecret JWTシークレットキー（環境変数から取得）
     */
    public AuthenticationService(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("JWT secret key cannot be null or blank");
        }
        // HS256には最低32バイトのキーが必要
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 bytes");
        }
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * パスワード認証
     * @param user ユーザーエンティティ
     * @param plainPassword 平文パスワード
     * @return 認証成功の場合true
     */
    public boolean authenticate(User user, String plainPassword) {
        if (user == null || plainPassword == null) {
            return false;
        }
        return user.verifyPassword(plainPassword);
    }

    /**
     * JWT生成
     * @param userId ユーザーID
     * @return JWT文字列
     */
    public String generateJwtToken(UserId userId) {
        Instant now = Instant.now();
        Instant expirationTime = now.plus(JWT_EXPIRATION_HOURS, ChronoUnit.HOURS);

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expirationTime))
            .signWith(jwtSecretKey, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * JWT検証
     * @param token JWT文字列
     * @return 検証成功の場合はUserId、失敗の場合はnull
     */
    public UserId validateJwtToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String userIdString = claims.getSubject();
            return UserId.of(userIdString);
        } catch (JwtException | IllegalArgumentException e) {
            // トークンが無効、期限切れ、または署名が不正
            return null;
        }
    }

    /**
     * JWTの有効期限を取得
     * @param token JWT文字列
     * @return 有効期限（Instant）、トークンが無効な場合はnull
     */
    public Instant getExpirationTime(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return claims.getExpiration().toInstant();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
