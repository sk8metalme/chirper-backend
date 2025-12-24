package com.chirper.presentation.controller;

import com.chirper.application.usecase.FollowUserUseCase;
import com.chirper.application.usecase.LikeTweetUseCase;
import com.chirper.application.usecase.RetweetUseCase;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.presentation.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * SocialController
 * ソーシャル機能のREST APIエンドポイント
 *
 * 責務:
 * - POST /api/v1/users/{userId}/follow (フォロー)
 * - DELETE /api/v1/users/{userId}/follow (フォロー解除)
 * - POST /api/v1/tweets/{tweetId}/like (いいね)
 * - DELETE /api/v1/tweets/{tweetId}/like (いいね解除)
 * - POST /api/v1/tweets/{tweetId}/retweet (リツイート)
 * - DELETE /api/v1/tweets/{tweetId}/retweet (リツイート解除)
 */
@RestController
@RequestMapping("/api/v1")
public class SocialController {

    private final FollowUserUseCase followUserUseCase;
    private final LikeTweetUseCase likeTweetUseCase;
    private final RetweetUseCase retweetUseCase;
    private final IFollowRepository followRepository;
    private final ILikeRepository likeRepository;
    private final IRetweetRepository retweetRepository;

    public SocialController(
        FollowUserUseCase followUserUseCase,
        LikeTweetUseCase likeTweetUseCase,
        RetweetUseCase retweetUseCase,
        IFollowRepository followRepository,
        ILikeRepository likeRepository,
        IRetweetRepository retweetRepository
    ) {
        this.followUserUseCase = followUserUseCase;
        this.likeTweetUseCase = likeTweetUseCase;
        this.retweetUseCase = retweetUseCase;
        this.followRepository = followRepository;
        this.likeRepository = likeRepository;
        this.retweetRepository = retweetRepository;
    }

    /**
     * ユーザーフォロー
     * POST /api/v1/users/{userId}/follow
     *
     * @param userId フォロー対象ユーザーID
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return 201 Created
     */
    @PostMapping("/users/{userId}/follow")
    public ResponseEntity<Void> followUser(
        @PathVariable String userId
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String followerUserIdString = authentication.getName();
        UserId followerUserId = UserId.of(followerUserIdString);

        // 2. フォロー対象ユーザーIDを生成
        UserId followedUserId = UserId.of(userId);

        // 3. フォローUseCaseを実行
        try {
            followUserUseCase.execute(followerUserId, followedUserId);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("BAD_REQUEST", e.getMessage());
        } catch (IllegalStateException e) {
            throw new BusinessException("CONFLICT", "既にフォロー済みです");
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * ユーザーフォロー解除
     * DELETE /api/v1/users/{userId}/follow
     *
     * @param userId フォロー解除対象ユーザーID
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return 204 No Content
     */
    @DeleteMapping("/users/{userId}/follow")
    public ResponseEntity<Void> unfollowUser(
        @PathVariable String userId
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String followerUserIdString = authentication.getName();
        UserId followerUserId = UserId.of(followerUserIdString);

        // 2. フォロー解除対象ユーザーIDを生成
        UserId followedUserId = UserId.of(userId);

        // 3. フォロー関係を削除
        followRepository.delete(followerUserId, followedUserId);

        return ResponseEntity.noContent().build();
    }

    /**
     * ツイートいいね
     * POST /api/v1/tweets/{tweetId}/like
     *
     * @param tweetId いいね対象ツイートID
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return 201 Created
     */
    @PostMapping("/tweets/{tweetId}/like")
    public ResponseEntity<Void> likeTweet(
        @PathVariable String tweetId
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UserId userId = UserId.of(userIdString);

        // 2. TweetIdを生成
        TweetId id = new TweetId(java.util.UUID.fromString(tweetId));

        // 3. いいねUseCaseを実行
        try {
            likeTweetUseCase.execute(userId, id);
        } catch (IllegalStateException e) {
            throw new BusinessException("CONFLICT", "既にいいね済みです");
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * ツイートいいね解除
     * DELETE /api/v1/tweets/{tweetId}/like
     *
     * @param tweetId いいね解除対象ツイートID
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return 204 No Content
     */
    @DeleteMapping("/tweets/{tweetId}/like")
    public ResponseEntity<Void> unlikeTweet(
        @PathVariable String tweetId
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UserId userId = UserId.of(userIdString);

        // 2. TweetIdを生成
        TweetId id = new TweetId(java.util.UUID.fromString(tweetId));

        // 3. いいね記録を削除
        likeRepository.delete(userId, id);

        return ResponseEntity.noContent().build();
    }

    /**
     * リツイート
     * POST /api/v1/tweets/{tweetId}/retweet
     *
     * @param tweetId リツイート対象ツイートID
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return 201 Created
     */
    @PostMapping("/tweets/{tweetId}/retweet")
    public ResponseEntity<Void> retweetTweet(
        @PathVariable String tweetId
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UserId userId = UserId.of(userIdString);

        // 2. TweetIdを生成
        TweetId id = new TweetId(java.util.UUID.fromString(tweetId));

        // 3. リツイートUseCaseを実行
        try {
            retweetUseCase.execute(userId, id);
        } catch (IllegalStateException e) {
            throw new BusinessException("CONFLICT", "既にリツイート済みです");
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * リツイート解除
     * DELETE /api/v1/tweets/{tweetId}/retweet
     *
     * @param tweetId リツイート解除対象ツイートID
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return 204 No Content
     */
    @DeleteMapping("/tweets/{tweetId}/retweet")
    public ResponseEntity<Void> unretweetTweet(
        @PathVariable String tweetId
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UserId userId = UserId.of(userIdString);

        // 2. TweetIdを生成
        TweetId id = new TweetId(java.util.UUID.fromString(tweetId));

        // 3. リツイート記録を削除
        retweetRepository.delete(userId, id);

        return ResponseEntity.noContent().build();
    }
}
