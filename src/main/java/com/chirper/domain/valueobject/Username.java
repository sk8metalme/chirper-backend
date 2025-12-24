package com.chirper.domain.valueobject;

/**
 * Username Value Object
 * ユーザー名を表す不変オブジェクト
 * ビジネスルール: 3-20文字の長さ制限
 */
public record Username(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 20;

    public Username(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Username must be between %d and %d characters, but got: %d",
                    MIN_LENGTH, MAX_LENGTH, trimmedValue.length())
            );
        }

        this.value = trimmedValue;
    }

    @Override
    public String toString() {
        return value;
    }
}
