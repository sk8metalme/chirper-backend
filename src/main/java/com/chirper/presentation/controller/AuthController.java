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
    private final com.chirper.domain.service.AuthenticationService authenticationService;

    public AuthController(
        RegisterUserUseCase registerUserUseCase,
        LoginUserUseCase loginUserUseCase,
        com.chirper.domain.service.AuthenticationService authenticationService
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = registerUserUseCase.execute(request.username(), request.email(), request.password());
        RegisterResponse response = new RegisterResponse(
            user.getId().value(),
            "ユーザー登録が完了しました"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var loginResult = loginUserUseCase.execute(request.username(), request.password());

        // トークンから実際の有効期限を取得
        java.time.Instant expiresAt = authenticationService.getExpirationTime(loginResult.token());
        if (expiresAt == null) {
            // トークンが無効な場合（通常発生しないはず）
            expiresAt = java.time.Instant.now().plusSeconds(3600);
        }

        LoginResponse response = new LoginResponse(
            loginResult.token(),
            loginResult.userId().value(),
            loginResult.username(),
            expiresAt
        );
        return ResponseEntity.ok(response);
    }
}
