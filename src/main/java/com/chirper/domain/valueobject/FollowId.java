package com.chirper.domain.valueobject;

import java.util.UUID;

/**
 * FollowId Value Object
 * フォロー関係の一意な識別子を表す不変オブジェクト
 */
public record FollowId(UUID value) {

    public FollowId {
        if (value == null) {
            throw new IllegalArgumentException("FollowId cannot be null");
        }
    }

    public static FollowId generate() {
        return new FollowId(UUID.randomUUID());
    }

    public static FollowId of(String value) {
        try {
            return new FollowId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid FollowId format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
