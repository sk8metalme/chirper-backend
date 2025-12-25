package com.chirper.presentation.dto.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginRequest バリデーションテスト")
class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("有効なLoginRequestはバリデーションエラーが発生しない")
    void validLoginRequest_shouldPassValidation() {
        // Given
        LoginRequest request = new LoginRequest(
            "validuser",
            "password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("usernameがnullの場合、バリデーションエラーが発生する")
    void usernameIsNull_shouldFailValidation() {
        // Given
        LoginRequest request = new LoginRequest(
            null,
            "password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    @DisplayName("usernameが空文字の場合、バリデーションエラーが発生する")
    void usernameIsBlank_shouldFailValidation() {
        // Given
        LoginRequest request = new LoginRequest(
            "",
            "password123"
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    @DisplayName("passwordがnullの場合、バリデーションエラーが発生する")
    void passwordIsNull_shouldFailValidation() {
        // Given
        LoginRequest request = new LoginRequest(
            "validuser",
            null
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    @DisplayName("passwordが空文字の場合、バリデーションエラーが発生する")
    void passwordIsBlank_shouldFailValidation() {
        // Given
        LoginRequest request = new LoginRequest(
            "validuser",
            ""
        );

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }
}
