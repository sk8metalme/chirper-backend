package com.chirper.domain.valueobject;

/**
 * TweetContent Value Object
 * ツイート本文を表す不変オブジェクト
 * ビジネスルール: 1-280文字の長さ制限
 */
public record TweetContent(String value) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 280;

    public TweetContent {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tweet content cannot be null or blank");
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Tweet content must be between %d and %d characters, but got: %d",
                    MIN_LENGTH, MAX_LENGTH, trimmedValue.length())
            );
        }

        value = trimmedValue;
    }

    @Override
    public String toString() {
        return value;
    }
}
