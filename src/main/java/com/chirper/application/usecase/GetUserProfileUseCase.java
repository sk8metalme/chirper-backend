package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Username;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * GetUserProfileUseCase
 * ユーザープロフィール取得ユースケース
 *
 * 責務:
 * - 指定されたユーザー名のユーザープロフィールを取得
 */
@Service
@Transactional(readOnly = true)
public class GetUserProfileUseCase {

    private final IUserRepository userRepository;

    public GetUserProfileUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザープロフィール取得を実行
     *
     * @param username ユーザー名
     * @return ユーザーエンティティ
     * @throws NullPointerException usernameがnullの場合
     * @throws IllegalArgumentException ユーザーが見つからない場合
     */
    public User execute(Username username) {
        if (username == null) {
            throw new NullPointerException("Username cannot be null");
        }

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
