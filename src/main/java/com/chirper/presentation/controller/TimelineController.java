package com.chirper.presentation.controller;

import com.chirper.application.usecase.GetTimelineUseCase;
import com.chirper.domain.valueobject.UserId;
import com.chirper.presentation.dto.tweet.TimelineResponse;
import com.chirper.presentation.dto.tweet.TweetResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TimelineController
 * タイムライン表示のREST APIエンドポイント
 *
 * 責務:
 * - GET /api/v1/timeline (タイムライン取得)
 */
@RestController
@RequestMapping("/api/v1/timeline")
@Validated
public class TimelineController {

    private final GetTimelineUseCase getTimelineUseCase;

    public TimelineController(GetTimelineUseCase getTimelineUseCase) {
        this.getTimelineUseCase = getTimelineUseCase;
    }

    /**
     * タイムライン取得
     * GET /api/v1/timeline
     *
     * @param page ページ番号（0始まり、デフォルト: 0）
     * @param size ページサイズ（デフォルト: 20、最大: 100）
     * @param authentication 認証情報（JWTから抽出されたユーザーID）
     * @return TimelineResponse (tweets: TweetDto[], totalPages: int)
     */
    @GetMapping
    public ResponseEntity<TimelineResponse> getTimeline(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        // 1. 認証情報からユーザーIDを取得
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        UserId userId = UserId.of(userIdString);

        // 2. タイムライン取得UseCaseを実行
        GetTimelineUseCase.TimelineResult result = getTimelineUseCase.execute(userId, page, size);

        // 3. レスポンスを作成
        List<TweetResponse> tweets = result.tweets().stream()
            .map(tweetWithStatus -> new TweetResponse(
                tweetWithStatus.tweet().getId().value(),
                tweetWithStatus.tweet().getUserId().value(),
                "", // username - 実装省略
                "", // displayName - 実装省略
                "", // avatarUrl - 実装省略
                tweetWithStatus.tweet().getContent().value(),
                tweetWithStatus.tweet().getCreatedAt(),
                0, // likesCount - 実装省略
                0, // retweetsCount - 実装省略
                tweetWithStatus.likedByCurrentUser(),
                tweetWithStatus.retweetedByCurrentUser()
            ))
            .collect(Collectors.toList());

        // 4. totalPagesの計算（簡略化: tweetsが空なら0、そうでなければ1）
        int totalPages = tweets.isEmpty() ? 0 : 1;

        TimelineResponse response = new TimelineResponse(tweets, totalPages);
        return ResponseEntity.ok(response);
    }
}
