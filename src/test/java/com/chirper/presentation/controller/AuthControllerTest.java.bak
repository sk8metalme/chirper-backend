package com.chirper.presentation.controller;

import com.chirper.application.usecase.LoginUserUseCase;
import com.chirper.application.usecase.RegisterUserUseCase;
import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import com.chirper.presentation.dto.auth.LoginRequest;
import com.chirper.presentation.dto.auth.LoginResponse;
import com.chirper.presentation.dto.auth.RegisterRequest;
import com.chirper.presentation.dto.auth.RegisterResponse;
import com.chirper.presentation.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController 統合テスト")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private com.chirper.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.chirper.infrastructure.security.JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/v1/auth/register - 有効なリクエストでユーザー登録が成功する")
    void registerUser_withValidRequest_shouldReturn201() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        UUID userId = UUID.randomUUID();
        User mockUser = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            "password123"
        );

        when(registerUserUseCase.execute(any(), any(), any())).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.message").value("ユーザー登録が完了しました"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - バリデーションエラーの場合400を返す")
    void registerUser_withInvalidRequest_shouldReturn400() throws Exception {
        // Given - usernameが短すぎる
        RegisterRequest request = new RegisterRequest("ab", "test@example.com", "password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - ユーザー名が重複している場合409を返す")
    void registerUser_withDuplicateUsername_shouldReturn409() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

        when(registerUserUseCase.execute(any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Username already exists: testuser"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("CONFLICT"))
            .andExpect(jsonPath("$.message").value("ユーザー名またはメールアドレスが既に存在します"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 有効な認証情報でログインが成功する")
    void loginUser_withValidCredentials_shouldReturn200() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        UUID userId = UUID.randomUUID();
        LoginUserUseCase.LoginResult loginResult = new LoginUserUseCase.LoginResult(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            new UserId(userId),
            "testuser"
        );

        when(loginUserUseCase.execute(any(), any())).thenReturn(loginResult);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 認証情報が誤っている場合401を返す")
    void loginUser_withInvalidCredentials_shouldReturn401() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(loginUserUseCase.execute(any(), any()))
            .thenThrow(new IllegalArgumentException("Authentication failed: Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
            .andExpect(jsonPath("$.message").value("認証に失敗しました"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - バリデーションエラーの場合400を返す")
    void loginUser_withInvalidRequest_shouldReturn400() throws Exception {
        // Given - usernameが空
        LoginRequest request = new LoginRequest("", "password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
