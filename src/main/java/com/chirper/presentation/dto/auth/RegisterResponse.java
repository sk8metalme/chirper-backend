package com.chirper.presentation.dto.auth;

import java.util.UUID;

public record RegisterResponse(
    UUID userId,
    String message
) {}
