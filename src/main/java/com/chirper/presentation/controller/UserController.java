package com.chirper.presentation.controller;

import com.chirper.application.usecase.GetUserProfileUseCase;
import com.chirper.application.usecase.UpdateProfileUseCase;
import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import com.chirper.presentation.dto.user.UpdateProfileRequest;
import com.chirper.presentation.dto.user.UpdateProfileResponse;
import com.chirper.presentation.dto.user.UserProfileResponse;
import com.chirper.presentation.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    public UserController(GetUserProfileUseCase getUserProfileUseCase, UpdateProfileUseCase updateProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
        @PathVariable String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // 1. 現在のユーザーIDを取得（認証されていない場合はnull）
        UserId currentUserId = null;
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String userIdString = authentication.getName();
            currentUserId = UserId.of(userIdString);
        }

        // 2. GetUserProfileUseCaseを実行
        GetUserProfileUseCase.UserProfileResult result;
        try {
            result = getUserProfileUseCase.execute(new Username(username), currentUserId, page, size);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("NOT_FOUND", "ユーザーが見つかりません");
        }

        // 3. ツイート一覧をTweetResponseに変換
        User user = result.user();
        List<com.chirper.presentation.dto.tweet.TweetResponse> tweetResponses = result.userTweets().stream()
            .map(tweet -> new com.chirper.presentation.dto.tweet.TweetResponse(
                tweet.getId().value(),
                tweet.getUserId().value(),
                user.getUsername().value(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                tweet.getContent().value(),
                tweet.getCreatedAt(),
                0, // likesCount - ユーザープロフィール画面では省略
                0, // retweetsCount - ユーザープロフィール画面では省略
                false, // likedByCurrentUser - ユーザープロフィール画面では省略
                false  // retweetedByCurrentUser - ユーザープロフィール画面では省略
            ))
            .collect(Collectors.toList());

        // 4. レスポンスを作成
        UserProfileResponse response = new UserProfileResponse(
            user.getId().value(),
            user.getUsername().value(),
            user.getDisplayName(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getCreatedAt(),
            (int) result.followersCount(),
            (int) result.followingCount(),
            result.followedByCurrentUser(),
            tweetResponses
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> updateProfile(
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("認証情報が取得できません");
        }
        String userIdString = authentication.getName();
        var userId = com.chirper.domain.valueobject.UserId.of(userIdString);
        updateProfileUseCase.execute(userId, request.displayName(), request.bio(), request.avatarUrl());

        UpdateProfileResponse response = new UpdateProfileResponse("プロフィールを更新しました");
        return ResponseEntity.ok(response);
    }
}
