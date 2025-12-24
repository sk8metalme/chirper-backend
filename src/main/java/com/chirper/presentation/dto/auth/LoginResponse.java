package com.chirper.presentation.dto.auth;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
    String token,
    UUID userId,
    String username,
    Instant expiresAt
) {}
