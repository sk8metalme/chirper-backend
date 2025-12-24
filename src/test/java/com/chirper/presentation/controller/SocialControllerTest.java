package com.chirper.presentation.controller;

import com.chirper.application.usecase.FollowUserUseCase;
import com.chirper.application.usecase.LikeTweetUseCase;
import com.chirper.application.usecase.RetweetUseCase;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.security.JwtUtil;
import com.chirper.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SocialControllerTest
 * SocialControllerのテストクラス（TDD RED Phase）
 *
 * テスト対象:
 * - POST /api/v1/users/{userId}/follow (フォロー)
 * - DELETE /api/v1/users/{userId}/follow (フォロー解除)
 * - POST /api/v1/tweets/{tweetId}/like (いいね)
 * - DELETE /api/v1/tweets/{tweetId}/like (いいね解除)
 * - POST /api/v1/tweets/{tweetId}/retweet (リツイート)
 * - DELETE /api/v1/tweets/{tweetId}/retweet (リツイート解除)
 */
@WebMvcTest(controllers = SocialController.class)
@AutoConfigureMockMvc(addFilters = false)

class SocialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowUserUseCase followUserUseCase;

    @MockBean
    private LikeTweetUseCase likeTweetUseCase;

    @MockBean
    private RetweetUseCase retweetUseCase;

    @MockBean
    private IFollowRepository followRepository;

    @MockBean
    private ILikeRepository likeRepository;

    @MockBean
    private IRetweetRepository retweetRepository;

    @MockBean
    private com.chirper.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    private UserId testUserId;
    private UserId targetUserId;
    private TweetId testTweetId;

    @BeforeEach
    void setUp() {
        testUserId = UserId.of(UUID.randomUUID().toString());
        targetUserId = UserId.of(UUID.randomUUID().toString());
        testTweetId = new TweetId(UUID.randomUUID());
    }

    // フォロー機能のテスト
    @Test
    @DisplayName("POST /api/v1/users/{userId}/follow - フォロー成功（201 Created）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void followUser_success() throws Exception {
        // Arrange
        doNothing().when(followUserUseCase).execute(any(UserId.class), eq(targetUserId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/" + targetUserId.value() + "/follow")
                .with(csrf()))
            .andExpect(status().isCreated());

        verify(followUserUseCase, times(1)).execute(any(UserId.class), eq(targetUserId));
    }

    @Test
    @DisplayName("POST /api/v1/users/{userId}/follow - 自分自身をフォロー（400 Bad Request）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void followUser_selfFollow() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Cannot follow yourself"))
            .when(followUserUseCase).execute(any(UserId.class), any(UserId.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/" + testUserId.value() + "/follow")
                .with(csrf()))
            .andExpect(status().isBadRequest());

        verify(followUserUseCase, times(1)).execute(any(UserId.class), any(UserId.class));
    }

    @Test
    @DisplayName("POST /api/v1/users/{userId}/follow - 既にフォロー済み（409 Conflict）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void followUser_alreadyFollowed() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Already following this user"))
            .when(followUserUseCase).execute(any(UserId.class), eq(targetUserId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/" + targetUserId.value() + "/follow")
                .with(csrf()))
            .andExpect(status().isConflict());

        verify(followUserUseCase, times(1)).execute(any(UserId.class), eq(targetUserId));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{userId}/follow - フォロー解除成功（204 No Content）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void unfollowUser_success() throws Exception {
        // Arrange
        doNothing().when(followRepository).delete(any(UserId.class), eq(targetUserId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/" + targetUserId.value() + "/follow")
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(followRepository, times(1)).delete(any(UserId.class), eq(targetUserId));
    }

    // いいね機能のテスト
    @Test
    @DisplayName("POST /api/v1/tweets/{tweetId}/like - いいね成功（201 Created）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void likeTweet_success() throws Exception {
        // Arrange
        doNothing().when(likeTweetUseCase).execute(any(UserId.class), eq(testTweetId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets/" + testTweetId.value() + "/like")
                .with(csrf()))
            .andExpect(status().isCreated());

        verify(likeTweetUseCase, times(1)).execute(any(UserId.class), eq(testTweetId));
    }

    @Test
    @DisplayName("POST /api/v1/tweets/{tweetId}/like - 既にいいね済み（409 Conflict）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void likeTweet_alreadyLiked() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Already liked this tweet"))
            .when(likeTweetUseCase).execute(any(UserId.class), eq(testTweetId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets/" + testTweetId.value() + "/like")
                .with(csrf()))
            .andExpect(status().isConflict());

        verify(likeTweetUseCase, times(1)).execute(any(UserId.class), eq(testTweetId));
    }

    @Test
    @DisplayName("DELETE /api/v1/tweets/{tweetId}/like - いいね解除成功（204 No Content）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void unlikeTweet_success() throws Exception {
        // Arrange
        doNothing().when(likeRepository).delete(any(UserId.class), eq(testTweetId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tweets/" + testTweetId.value() + "/like")
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(likeRepository, times(1)).delete(any(UserId.class), eq(testTweetId));
    }

    // リツイート機能のテスト
    @Test
    @DisplayName("POST /api/v1/tweets/{tweetId}/retweet - リツイート成功（201 Created）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void retweetTweet_success() throws Exception {
        // Arrange
        doNothing().when(retweetUseCase).execute(any(UserId.class), eq(testTweetId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets/" + testTweetId.value() + "/retweet")
                .with(csrf()))
            .andExpect(status().isCreated());

        verify(retweetUseCase, times(1)).execute(any(UserId.class), eq(testTweetId));
    }

    @Test
    @DisplayName("POST /api/v1/tweets/{tweetId}/retweet - 既にリツイート済み（409 Conflict）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void retweetTweet_alreadyRetweeted() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Already retweeted this tweet"))
            .when(retweetUseCase).execute(any(UserId.class), eq(testTweetId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets/" + testTweetId.value() + "/retweet")
                .with(csrf()))
            .andExpect(status().isConflict());

        verify(retweetUseCase, times(1)).execute(any(UserId.class), eq(testTweetId));
    }

    @Test
    @DisplayName("DELETE /api/v1/tweets/{tweetId}/retweet - リツイート解除成功（204 No Content）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void unretweetTweet_success() throws Exception {
        // Arrange
        doNothing().when(retweetRepository).delete(any(UserId.class), eq(testTweetId));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tweets/" + testTweetId.value() + "/retweet")
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(retweetRepository, times(1)).delete(any(UserId.class), eq(testTweetId));
    }

    // 認証エラーテスト
    @Test
    @Disabled("@AutoConfigureMockMvc(addFilters = false)のため、認証テストは統合テストで実装すべき")
    @DisplayName("POST /api/v1/users/{userId}/follow - 認証なし（401 Unauthorized）")
    void followUser_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/users/" + targetUserId.value() + "/follow")
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verify(followUserUseCase, never()).execute(any(), any());
    }

    @Test
    @Disabled("@AutoConfigureMockMvc(addFilters = false)のため、認証テストは統合テストで実装すべき")
    @DisplayName("POST /api/v1/tweets/{tweetId}/like - 認証なし（401 Unauthorized）")
    void likeTweet_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/tweets/" + testTweetId.value() + "/like")
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verify(likeTweetUseCase, never()).execute(any(), any());
    }

    @Test
    @Disabled("@AutoConfigureMockMvc(addFilters = false)のため、認証テストは統合テストで実装すべき")
    @DisplayName("POST /api/v1/tweets/{tweetId}/retweet - 認証なし（401 Unauthorized）")
    void retweetTweet_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/tweets/" + testTweetId.value() + "/retweet")
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verify(retweetUseCase, never()).execute(any(), any());
    }
}
