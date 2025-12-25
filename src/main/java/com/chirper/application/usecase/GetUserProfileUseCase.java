package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * GetUserProfileUseCase
 * ユーザープロフィール取得ユースケース
 *
 * 責務:
 * - 指定されたユーザー名のユーザープロフィールを取得
 * - フォロワー数、フォロー数、フォロー状態を取得
 * - ユーザーのツイート一覧を取得
 */
@Service
@Transactional(readOnly = true)
public class GetUserProfileUseCase {

    private final IUserRepository userRepository;
    private final IFollowRepository followRepository;
    private final ITweetRepository tweetRepository;

    public GetUserProfileUseCase(
        IUserRepository userRepository,
        IFollowRepository followRepository,
        ITweetRepository tweetRepository
    ) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.tweetRepository = tweetRepository;
    }

    /**
     * ユーザープロフィール取得を実行
     *
     * @param username ユーザー名
     * @param currentUserId 現在のユーザーID（フォロー状態確認用、nullの場合は未ログイン）
     * @param page ツイート一覧のページ番号（0始まり）
     * @param size ツイート一覧のページサイズ
     * @return ユーザープロフィール結果
     * @throws NullPointerException usernameがnullの場合
     * @throws EntityNotFoundException ユーザーが見つからない場合
     */
    public UserProfileResult execute(Username username, UserId currentUserId, int page, int size) {
        if (username == null) {
            throw new NullPointerException("Username cannot be null");
        }

        // ユーザー情報を取得
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + username.value()));

        // フォロワー数とフォロー数を取得
        long followersCount = followRepository.countFollowers(user.getId());
        long followingCount = followRepository.countFollowing(user.getId());

        // 現在のユーザーがフォローしているかチェック
        boolean followedByCurrentUser = false;
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            followedByCurrentUser = followRepository.existsByFollowerAndFollowed(currentUserId, user.getId());
        }

        // ユーザーのツイート一覧を取得
        List<Tweet> userTweets = tweetRepository.findByUserId(user.getId(), page, size);

        return new UserProfileResult(user, followersCount, followingCount, followedByCurrentUser, userTweets);
    }

    /**
     * ユーザープロフィール結果
     *
     * @param user ユーザー情報
     * @param followersCount フォロワー数
     * @param followingCount フォロー数
     * @param followedByCurrentUser 現在のユーザーがフォローしているか
     * @param userTweets ユーザーのツイート一覧
     */
    public record UserProfileResult(
        User user,
        long followersCount,
        long followingCount,
        boolean followedByCurrentUser,
        List<Tweet> userTweets
    ) {}
}
