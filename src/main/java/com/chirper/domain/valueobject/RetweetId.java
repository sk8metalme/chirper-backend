package com.chirper.domain.valueobject;

import java.util.UUID;

/**
 * RetweetId Value Object
 * リツイートの一意な識別子を表す不変オブジェクト
 */
public record RetweetId(UUID value) {

    public RetweetId {
        if (value == null) {
            throw new IllegalArgumentException("RetweetId cannot be null");
        }
    }

    public static RetweetId generate() {
        return new RetweetId(UUID.randomUUID());
    }

    public static RetweetId of(String value) {
        try {
            return new RetweetId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid RetweetId format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
