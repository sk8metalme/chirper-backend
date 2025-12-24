package com.chirper.presentation.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Validation error: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ErrorResponse.FieldError(
                fieldError.getField(),
                fieldError.getDefaultMessage()
            ));
        }

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "リクエストのバリデーションに失敗しました",
            fieldErrors,
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            fieldErrors.add(new ErrorResponse.FieldError(
                fieldName,
                violation.getMessage()
            ));
        }

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "リクエストのバリデーションに失敗しました",
            fieldErrors,
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        logger.warn("Missing request parameter: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new ErrorResponse.FieldError(
            ex.getParameterName(),
            ex.getParameterName() + "パラメータは必須です"
        ));

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "リクエストのバリデーションに失敗しました",
            fieldErrors,
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        logger.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        HttpStatus status = switch (ex.getErrorCode()) {
            case "CONFLICT" -> HttpStatus.CONFLICT;
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "BAD_REQUEST" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            List.of(),
            Instant.now()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Internal error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "内部サーバーエラーが発生しました",
            List.of(),
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
