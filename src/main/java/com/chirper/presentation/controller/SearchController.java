package com.chirper.presentation.controller;

import com.chirper.application.usecase.SearchUseCase;
import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.presentation.dto.tweet.TweetResponse;
import com.chirper.presentation.dto.user.UserSearchResponse;
import com.chirper.presentation.exception.BusinessException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SearchController
 * 検索APIエンドポイント
 *
 * 責務:
 * - GET /api/v1/search (ユーザー・ツイート検索)
 */
@RestController
@RequestMapping("/api/v1/search")
@Validated
public class SearchController {

    private final SearchUseCase searchUseCase;

    public SearchController(SearchUseCase searchUseCase) {
        this.searchUseCase = searchUseCase;
    }

    /**
     * ユーザー・ツイート検索
     * GET /api/v1/search
     *
     * @param query 検索キーワード（2文字以上）
     * @param page ページ番号（0始まり、デフォルト: 0）
     * @param size ページサイズ（デフォルト: 20、最大: 100）
     * @return SearchResponse (users: UserSearchResponse[], tweets: TweetResponse[])
     */
    @GetMapping
    public ResponseEntity<SearchResponse> search(
        @RequestParam @NotBlank(message = "検索キーワードは必須です")
        @Size(min = 2, message = "検索キーワードは2文字以上である必要があります") String query,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        // 1. 検索UseCaseを実行
        SearchUseCase.SearchResult result;
        try {
            result = searchUseCase.execute(query, page, size);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("BAD_REQUEST", e.getMessage());
        }

        // 2. レスポンスを作成
        List<UserSearchResponse> users = result.users().stream()
            .map(this::toUserSearchResponse)
            .collect(Collectors.toList());

        List<TweetResponse> tweets = result.tweets().stream()
            .map(this::toTweetResponse)
            .collect(Collectors.toList());

        SearchResponse response = new SearchResponse(users, tweets);
        return ResponseEntity.ok(response);
    }

    /**
     * UserエンティティをUserSearchResponseに変換
     */
    private UserSearchResponse toUserSearchResponse(User user) {
        return new UserSearchResponse(
            user.getId().value(),
            user.getUsername().value(),
            user.getDisplayName(),
            user.getBio(),
            user.getAvatarUrl()
        );
    }

    /**
     * TweetエンティティをTweetResponseに変換
     */
    private TweetResponse toTweetResponse(Tweet tweet) {
        return new TweetResponse(
            tweet.getId().value(),
            tweet.getUserId().value(),
            "", // username - 実装省略
            "", // displayName - 実装省略
            "", // avatarUrl - 実装省略
            tweet.getContent().value(),
            tweet.getCreatedAt(),
            0, // likesCount - 実装省略
            0, // retweetsCount - 実装省略
            false, // likedByCurrentUser - 実装省略
            false  // retweetedByCurrentUser - 実装省略
        );
    }

    /**
     * 検索レスポンス
     */
    public record SearchResponse(
        List<UserSearchResponse> users,
        List<TweetResponse> tweets
    ) {}
}
