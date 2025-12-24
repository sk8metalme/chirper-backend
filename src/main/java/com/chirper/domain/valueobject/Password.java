package com.chirper.domain.valueobject;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Password Value Object
 * パスワードハッシュを表す不変オブジェクト
 * ビジネスルール: bcryptハッシュ化（コスト係数10）
 */
public record Password(String hashedValue) {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    public Password {
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
    }

    /**
     * 平文パスワードからPassword Value Objectを生成
     * @param plainPassword 平文パスワード
     * @return ハッシュ化されたPassword
     */
    public static Password fromPlainText(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Plain password cannot be null or blank");
        }

        String hashed = PASSWORD_ENCODER.encode(plainPassword);
        return new Password(hashed);
    }

    /**
     * 平文パスワードがこのハッシュと一致するか検証
     * @param plainPassword 検証する平文パスワード
     * @return 一致する場合true
     */
    public boolean matches(String plainPassword) {
        if (plainPassword == null) {
            return false;
        }
        return PASSWORD_ENCODER.matches(plainPassword, hashedValue);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
