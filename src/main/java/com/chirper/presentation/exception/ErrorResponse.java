package com.chirper.presentation.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<FieldError> details,
    Instant timestamp
) {
    public record FieldError(
        String field,
        String message
    ) {}
}
