package com.chirper.domain.valueobject;

import java.util.UUID;

/**
 * UserId Value Object
 * ユーザーの一意な識別子を表す不変オブジェクト
 */
public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String value) {
        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UserId format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
