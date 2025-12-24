package com.chirper.domain.valueobject;

import java.util.UUID;

/**
 * TweetId Value Object
 * ツイートの一意な識別子を表す不変オブジェクト
 */
public record TweetId(UUID value) {

    public TweetId {
        if (value == null) {
            throw new IllegalArgumentException("TweetId cannot be null");
        }
    }

    public static TweetId generate() {
        return new TweetId(UUID.randomUUID());
    }

    public static TweetId of(String value) {
        try {
            return new TweetId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid TweetId format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
