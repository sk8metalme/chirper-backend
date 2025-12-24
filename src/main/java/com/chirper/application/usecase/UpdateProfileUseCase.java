package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UpdateProfileUseCase
 * プロフィール更新のユースケース
 *
 * 責務:
 * - プロフィール情報(displayName、bio、avatarUrl)を更新
 * - ユーザーが存在することを確認
 * - トランザクション境界を管理
 */
@Service
@Transactional
public class UpdateProfileUseCase {

    private final IUserRepository userRepository;

    public UpdateProfileUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * プロフィール更新を実行
     * @param userId ユーザーID
     * @param displayName 表示名（オプション）
     * @param bio 自己紹介（オプション）
     * @param avatarUrl アバターURL（オプション）
     * @return 更新されたUser Entity
     * @throws IllegalArgumentException ユーザーが存在しない場合
     * @throws NullPointerException userIdがnullの場合
     */
    public User execute(UserId userId, String displayName, String bio, String avatarUrl) {
        // 1. バリデーション
        if (userId == null) {
            throw new NullPointerException("UserId cannot be null");
        }

        // 2. ユーザーを検索
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        User user = userOptional.get();

        // 3. プロフィール情報を更新
        user.updateProfile(displayName, bio, avatarUrl);

        // 4. データベースに永続化
        return userRepository.save(user);
    }
}
