package com.chirper.presentation.controller;

import com.chirper.application.usecase.GetFollowersUseCase;
import com.chirper.application.usecase.GetFollowingUseCase;
import com.chirper.application.usecase.GetUserProfileUseCase;
import com.chirper.application.usecase.UpdateProfileUseCase;
import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import com.chirper.presentation.dto.user.FollowListResponse;
import com.chirper.presentation.dto.user.FollowUserResponse;
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
    private final GetFollowersUseCase getFollowersUseCase;
    private final GetFollowingUseCase getFollowingUseCase;

    public UserController(
        GetUserProfileUseCase getUserProfileUseCase,
        UpdateProfileUseCase updateProfileUseCase,
        GetFollowersUseCase getFollowersUseCase,
        GetFollowingUseCase getFollowingUseCase
    ) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.getFollowersUseCase = getFollowersUseCase;
        this.getFollowingUseCase = getFollowingUseCase;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
        @PathVariable String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        UserId currentUserId = getCurrentUserIdOrNull();

        // GetUserProfileUseCaseを実行
        GetUserProfileUseCase.UserProfileResult result;
        try {
            result = getUserProfileUseCase.execute(new Username(username), currentUserId, page, size);
        } catch (EntityNotFoundException e) {
            throw new BusinessException("NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_PARAMETER", "パラメータが不正です: " + e.getMessage());
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

    /**
     * 現在のユーザーIDを取得（認証されていない場合はnull）
     * @return 現在のユーザーID（未認証の場合はnull）
     */
    private UserId getCurrentUserIdOrNull() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (authentication != null
            && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return UserId.of(authentication.getName());
        }
        return null;
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<FollowListResponse> getFollowers(
        @PathVariable String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        UserId currentUserId = getCurrentUserIdOrNull();

        // GetFollowersUseCaseを実行
        GetFollowersUseCase.FollowersResult result;
        try {
            result = getFollowersUseCase.execute(new Username(username), currentUserId, page, size);
        } catch (EntityNotFoundException e) {
            throw new BusinessException("NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_PARAMETER", "パラメータが不正です: " + e.getMessage());
        }

        // レスポンスに変換
        List<FollowUserResponse> userResponses = result.followers().stream()
            .map(followerInfo -> new FollowUserResponse(
                followerInfo.user().getId().value(),
                followerInfo.user().getUsername().value(),
                followerInfo.user().getDisplayName(),
                followerInfo.user().getAvatarUrl(),
                followerInfo.followedByCurrentUser()
            ))
            .collect(Collectors.toList());

        FollowListResponse response = new FollowListResponse(
            userResponses,
            result.currentPage(),
            result.totalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<FollowListResponse> getFollowing(
        @PathVariable String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        UserId currentUserId = getCurrentUserIdOrNull();

        // GetFollowingUseCaseを実行
        GetFollowingUseCase.FollowingResult result;
        try {
            result = getFollowingUseCase.execute(new Username(username), currentUserId, page, size);
        } catch (EntityNotFoundException e) {
            throw new BusinessException("NOT_FOUND", e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_PARAMETER", "パラメータが不正です: " + e.getMessage());
        }

        // レスポンスに変換
        List<FollowUserResponse> userResponses = result.following().stream()
            .map(followingInfo -> new FollowUserResponse(
                followingInfo.user().getId().value(),
                followingInfo.user().getUsername().value(),
                followingInfo.user().getDisplayName(),
                followingInfo.user().getAvatarUrl(),
                followingInfo.followedByCurrentUser()
            ))
            .collect(Collectors.toList());

        FollowListResponse response = new FollowListResponse(
            userResponses,
            result.currentPage(),
            result.totalPages()
        );

        return ResponseEntity.ok(response);
    }
}
