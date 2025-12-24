package com.chirper.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "ユーザー名は必須です")
    String username,

    @NotBlank(message = "パスワードは必須です")
    String password
) {}
