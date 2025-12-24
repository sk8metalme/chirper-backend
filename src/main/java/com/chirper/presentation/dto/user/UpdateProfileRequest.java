package com.chirper.presentation.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 50, message = "表示名は50文字以下である必要があります")
    String displayName,

    @Size(max = 160, message = "自己紹介は160文字以下である必要があります")
    String bio,

    String avatarUrl
) {}
