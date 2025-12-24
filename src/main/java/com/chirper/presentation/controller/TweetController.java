package com.chirper.presentation.controller;

import com.chirper.application.usecase.CreateTweetUseCase;
import com.chirper.application.usecase.DeleteTweetUseCase;
import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.presentation.dto.tweet.CreateTweetRequest;
import com.chirper.presentation.dto.tweet.CreateTweetResponse;
import com.chirper.presentation.dto.tweet.TweetResponse;
import com.chirper.presentation.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * TweetController
 * ツイート管理のREST APIエンドポイント
 *
 * 責務:
 * - POST /api/v1/tweets (ツイート投稿)
 * - GET /api/v1/tweets/{tweetId} (ツイート取得)
 * - DELETE /api/v1/tweets/{tweetId} (ツイート削除)
 */
@RestController
@RequestMapping("/api/v1/tweets")
public class TweetController {

    private final CreateTweetUseCase createTweetUseCase;
    private final DeleteTweetUseCase deleteTweetUseCase;
    private final ITweetRepository tweetRepository;
    private final ILikeRepository likeRepository;
    private final IRetweetRepository retweetRepository;

    public TweetController(
        CreateTweetUseCase createTweetUseCase,
        DeleteTweetUseCase deleteTweetUseCase,
        ITweetRepository tweetRepository,
        ILikeRepository likeRepository,
        IRetweetRepository retweetRepository
    ) {
        this.createTweetUseCase = createTweetUseCase;
        this.deleteTweetUseCase = deleteTweetUseCase;
        this.tweetRepository = tweetRepository;
        this.likeRepository = likeRepository;
        this.retweetRepository = retweetRepository;
    }

    /**
     * ツイート投稿
     * POST /api/v1/tweets
     *
     * @param request ツイート投稿リクエスト
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return CreateTweetResponse (tweetId, createdAt)
     */
    @PostMapping
    public ResponseEntity<CreateTweetResponse> createTweet(
        @Valid @RequestBody CreateTweetRequest request
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UserId userId = UserId.of(userIdString);

        // 2. ツイート投稿UseCaseを実行
        Tweet tweet = createTweetUseCase.execute(userId, request.content());

        // 3. レスポンスを作成
        CreateTweetResponse response = new CreateTweetResponse(
            tweet.getId().value(),
            tweet.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ツイート取得
     * GET /api/v1/tweets/{tweetId}
     *
     * @param tweetId ツイートID
     * @return TweetResponse (tweetId, userId, username, displayName, avatarUrl, content, createdAt, likesCount, retweetsCount, likedByCurrentUser, retweetedByCurrentUser)
     */
    @GetMapping("/{tweetId}")
    public ResponseEntity<TweetResponse> getTweet(@PathVariable String tweetId) {
        // 1. TweetIdを生成
        TweetId id = new TweetId(java.util.UUID.fromString(tweetId));

        // 2. ツイートを取得
        Tweet tweet = tweetRepository.findById(id)
            .orElseThrow(() -> new BusinessException("NOT_FOUND", "ツイートが見つかりません"));

        // 3. いいね数・リツイート数を取得
        int likesCount = likeRepository.findByTweetId(id).size();
        int retweetsCount = retweetRepository.findByTweetId(id).size();

        // 4. レスポンスを作成（ユーザー情報は簡略化、likedByCurrentUser/retweetedByCurrentUserはfalse固定）
        TweetResponse response = new TweetResponse(
            tweet.getId().value(),
            tweet.getUserId().value(),
            "", // username - 実装省略
            "", // displayName - 実装省略
            "", // avatarUrl - 実装省略
            tweet.getContent().value(),
            tweet.getCreatedAt(),
            likesCount,
            retweetsCount,
            false, // likedByCurrentUser - 実装省略
            false  // retweetedByCurrentUser - 実装省略
        );

        return ResponseEntity.ok(response);
    }

    /**
     * ツイート削除
     * DELETE /api/v1/tweets/{tweetId}
     *
     * @param tweetId ツイートID
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return 204 No Content
     */
    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Void> deleteTweet(
        @PathVariable String tweetId
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UserId userId = UserId.of(userIdString);

        // 2. TweetIdを生成
        TweetId id = new TweetId(java.util.UUID.fromString(tweetId));

        // 3. ツイート削除UseCaseを実行
        try {
            deleteTweetUseCase.execute(id, userId);
        } catch (SecurityException e) {
            throw new BusinessException("FORBIDDEN", "投稿者以外は削除できません");
        } catch (IllegalArgumentException e) {
            throw new BusinessException("NOT_FOUND", "ツイートが見つかりません");
        }

        return ResponseEntity.noContent().build();
    }
}
