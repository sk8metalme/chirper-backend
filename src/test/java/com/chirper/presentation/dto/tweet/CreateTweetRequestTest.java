package com.chirper.presentation.dto.tweet;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateTweetRequest バリデーションテスト")
class CreateTweetRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("有効なCreateTweetRequestはバリデーションエラーが発生しない")
    void validCreateTweetRequest_shouldPassValidation() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest("Hello, Chirper!");

        // When
        Set<ConstraintViolation<CreateTweetRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("contentがnullの場合、バリデーションエラーが発生する")
    void contentIsNull_shouldFailValidation() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest(null);

        // When
        Set<ConstraintViolation<CreateTweetRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    @DisplayName("contentが空文字の場合、バリデーションエラーが発生する")
    void contentIsBlank_shouldFailValidation() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest("");

        // When
        Set<ConstraintViolation<CreateTweetRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    @DisplayName("contentが280文字を超える場合、バリデーションエラーが発生する")
    void contentTooLong_shouldFailValidation() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest("a".repeat(281));

        // When
        Set<ConstraintViolation<CreateTweetRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
        assertThat(violations.iterator().next().getMessage()).contains("280");
    }

    @Test
    @DisplayName("contentが280文字ちょうどの場合、バリデーションエラーが発生しない")
    void contentExactly280_shouldPassValidation() {
        // Given
        CreateTweetRequest request = new CreateTweetRequest("a".repeat(280));

        // When
        Set<ConstraintViolation<CreateTweetRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}
