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

@DisplayName("RegisterRequest バリデーションテスト")
class RegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("有効なRegisterRequestはバリデーションエラーが発生しない")
    void validRegisterRequest_shouldPassValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "validuser",
            "valid@example.com",
            "password123"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("usernameがnullの場合、バリデーションエラーが発生する")
    void usernameIsNull_shouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            null,
            "valid@example.com",
            "password123"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    @DisplayName("usernameが3文字未満の場合、バリデーションエラーが発生する")
    void usernameTooShort_shouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "ab",
            "valid@example.com",
            "password123"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
        assertThat(violations.iterator().next().getMessage()).contains("3");
    }

    @Test
    @DisplayName("usernameが20文字を超える場合、バリデーションエラーが発生する")
    void usernameTooLong_shouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "a".repeat(21),
            "valid@example.com",
            "password123"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
        assertThat(violations.iterator().next().getMessage()).contains("20");
    }

    @Test
    @DisplayName("emailがnullの場合、バリデーションエラーが発生する")
    void emailIsNull_shouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "validuser",
            null,
            "password123"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    @DisplayName("emailが不正な形式の場合、バリデーションエラーが発生する")
    void emailInvalidFormat_shouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "validuser",
            "invalid-email",
            "password123"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    @DisplayName("passwordがnullの場合、バリデーションエラーが発生する")
    void passwordIsNull_shouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "validuser",
            "valid@example.com",
            null
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    @DisplayName("passwordが8文字未満の場合、バリデーションエラーが発生する")
    void passwordTooShort_shouldFailValidation() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "validuser",
            "valid@example.com",
            "pass"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("password");
        assertThat(violations.iterator().next().getMessage()).contains("8");
    }
}
