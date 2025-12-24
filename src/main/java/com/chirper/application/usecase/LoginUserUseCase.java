package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.service.AuthenticationService;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * LoginUserUseCase
 * ユーザーログインのユースケース
 *
 * 責務:
 * - IUserRepository.findByUsername()でユーザー取得
 * - AuthenticationService.authenticate()でパスワード検証
 * - 認証成功時、AuthenticationService.generateJwtToken()でJWT発行
 * - トランザクション境界を管理
 */
@Service
@Transactional(readOnly = true)
public class LoginUserUseCase {

    private final IUserRepository userRepository;
    private final AuthenticationService authenticationService;

    public LoginUserUseCase(IUserRepository userRepository, AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    /**
     * ログインを実行
     * @param usernameString ユーザー名
     * @param plainPassword 平文パスワード
     * @return ログイン結果（JWTトークン、ユーザーID、ユーザー名）
     * @throws IllegalArgumentException 認証失敗の場合
     */
    public LoginResult execute(String usernameString, String plainPassword) {
        // 1. Value Objectsを生成（バリデーション）
        Username username = new Username(usernameString);

        // 2. ユーザーを検索
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Authentication failed: Invalid credentials");
        }

        User user = userOptional.get();

        // 3. パスワードを検証
        boolean authenticated = authenticationService.authenticate(user, plainPassword);
        if (!authenticated) {
            throw new IllegalArgumentException("Authentication failed: Invalid credentials");
        }

        // 4. JWTトークンを生成
        String token = authenticationService.generateJwtToken(user.getId());

        // 5. ログイン結果を返却
        return new LoginResult(token, user.getId(), usernameString);
    }

    /**
     * ログイン結果
     * @param token JWTトークン
     * @param userId ユーザーID
     * @param username ユーザー名
     */
    public record LoginResult(String token, UserId userId, String username) {}
}
