package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.exception.DuplicateEntityException;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.Username;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * RegisterUserUseCase
 * ユーザー登録のユースケース
 *
 * 責務:
 * - ユーザー名の一意性をチェック
 * - メールアドレスの一意性をチェック
 * - User Entityを生成（パスワードハッシュ化を含む）
 * - IUserRepositoryを使用してデータベースに永続化
 * - トランザクション境界を管理
 */
@Service
@Transactional
public class RegisterUserUseCase {

    private final IUserRepository userRepository;

    public RegisterUserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー登録を実行
     * @param usernameString ユーザー名
     * @param emailString メールアドレス
     * @param plainPassword 平文パスワード
     * @return 登録されたUser Entity
     * @throws DuplicateEntityException ユーザー名またはメールアドレスが既に存在する場合
     */
    public User execute(String usernameString, String emailString, String plainPassword) {
        // 1. Value Objectsを生成（バリデーション）
        Username username = new Username(usernameString);
        Email email = new Email(emailString);

        // 2. ユーザー名の一意性をチェック
        Optional<User> existingUserByUsername = userRepository.findByUsername(username);
        if (existingUserByUsername.isPresent()) {
            throw new DuplicateEntityException("ユーザー名は既に使用されています: " + username.value());
        }

        // 3. メールアドレスの一意性をチェック
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            throw new DuplicateEntityException("メールアドレスは既に使用されています: " + email.value());
        }

        // 4. User Entityを生成（パスワードハッシュ化を含む）
        User newUser = User.create(username, email, plainPassword);

        // 5. データベースに永続化
        return userRepository.save(newUser);
    }
}
