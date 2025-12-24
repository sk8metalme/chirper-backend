package com.chirper.presentation.controller;

import com.chirper.application.usecase.LoginUserUseCase;
import com.chirper.application.usecase.RegisterUserUseCase;
import com.chirper.presentation.dto.auth.LoginRequest;
import com.chirper.presentation.dto.auth.LoginResponse;
import com.chirper.presentation.dto.auth.RegisterRequest;
import com.chirper.presentation.dto.auth.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            var user = registerUserUseCase.execute(request.username(), request.email(), request.password());
            RegisterResponse response = new RegisterResponse(
                user.getId().value(),
                "ユーザー登録が完了しました"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already exists")) {
                throw new com.chirper.presentation.exception.BusinessException("CONFLICT", "ユーザー名またはメールアドレスが既に存在します");
            }
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            var loginResult = loginUserUseCase.execute(request.username(), request.password());
            LoginResponse response = new LoginResponse(
                loginResult.token(),
                loginResult.userId().value(),
                loginResult.username(),
                java.time.Instant.now().plusSeconds(3600)
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new com.chirper.presentation.exception.BusinessException("UNAUTHORIZED", "認証に失敗しました");
        }
    }
}
