package com.chirper.presentation.dto.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateProfileRequest バリデーションテスト")
class UpdateProfileRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("有効なUpdateProfileRequestはバリデーションエラーが発生しない")
    void validUpdateProfileRequest_shouldPassValidation() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest(
            "John Doe",
            "Hello, I'm John!",
            "https://example.com/avatar.jpg"
        );

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("すべてのフィールドがnullの場合もバリデーションエラーが発生しない（任意項目）")
    void allFieldsNull_shouldPassValidation() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null);

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("displayNameが50文字を超える場合、バリデーションエラーが発生する")
    void displayNameTooLong_shouldFailValidation() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest(
            "a".repeat(51),
            "Bio text",
            "https://example.com/avatar.jpg"
        );

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("displayName");
    }

    @Test
    @DisplayName("bioが160文字を超える場合、バリデーションエラーが発生する")
    void bioTooLong_shouldFailValidation() {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest(
            "John Doe",
            "a".repeat(161),
            "https://example.com/avatar.jpg"
        );

        // When
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("bio");
    }
}
