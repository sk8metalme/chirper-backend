package com.chirper.domain.valueobject;

import java.util.UUID;

/**
 * LikeId Value Object
 * いいねの一意な識別子を表す不変オブジェクト
 */
public record LikeId(UUID value) {

    public LikeId {
        if (value == null) {
            throw new IllegalArgumentException("LikeId cannot be null");
        }
    }

    public static LikeId generate() {
        return new LikeId(UUID.randomUUID());
    }

    public static LikeId of(String value) {
        try {
            return new LikeId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid LikeId format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
