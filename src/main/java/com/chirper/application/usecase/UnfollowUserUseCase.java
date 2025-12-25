package com.chirper.application.usecase;

import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UnfollowUserUseCase
 * ユーザーフォロー解除ユースケース
 *
 * 責務:
 * - フォロー関係を削除
 */
@Service
@Transactional
public class UnfollowUserUseCase {

    private final IFollowRepository followRepository;

    public UnfollowUserUseCase(IFollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    /**
     * フォロー解除を実行
     *
     * @param followerUserId フォローを解除するユーザーID
     * @param followedUserId フォロー解除対象ユーザーID
     * @throws NullPointerException followerUserIdまたはfollowedUserIdがnullの場合
     */
    public void execute(UserId followerUserId, UserId followedUserId) {
        if (followerUserId == null) {
            throw new NullPointerException("FollowerUserId cannot be null");
        }
        if (followedUserId == null) {
            throw new NullPointerException("FollowedUserId cannot be null");
        }

        followRepository.delete(followerUserId, followedUserId);
    }
}
