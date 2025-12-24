package com.chirper.presentation.controller;

import com.chirper.application.usecase.GetUserProfileUseCase;
import com.chirper.application.usecase.UpdateProfileUseCase;
import com.chirper.domain.entity.User;
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
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        // 1. GetUserProfileUseCaseを実行
        User user;
        try {
            user = getUserProfileUseCase.execute(new Username(username));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("NOT_FOUND", "ユーザーが見つかりません");
        }

        // 2. レスポンスを作成
        UserProfileResponse response = new UserProfileResponse(
            user.getId().value(),
            user.getUsername().value(),
            user.getDisplayName(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getCreatedAt(),
            0, // followersCount - 実装省略
            0, // followingCount - 実装省略
            false, // followedByCurrentUser - 実装省略
            List.of() // userTweets - 実装省略
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UpdateProfileResponse> updateProfile(
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        var userId = com.chirper.domain.valueobject.UserId.of(userIdString);
        updateProfileUseCase.execute(userId, request.displayName(), request.bio(), request.avatarUrl());

        UpdateProfileResponse response = new UpdateProfileResponse("プロフィールを更新しました");
        return ResponseEntity.ok(response);
    }
}
