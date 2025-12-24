package com.chirper.domain.valueobject;

import java.util.regex.Pattern;

/**
 * Email Value Object
 * メールアドレスを表す不変オブジェクト
 * ビジネスルール: 有効なメールアドレス形式
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        String trimmedValue = value.trim();
        if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + trimmedValue);
        }

        value = trimmedValue;
    }

    @Override
    public String toString() {
        return value;
    }
}
