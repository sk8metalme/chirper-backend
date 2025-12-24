package com.chirper.presentation.controller;

import com.chirper.application.usecase.CreateTweetUseCase;
import com.chirper.application.usecase.DeleteTweetUseCase;
import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import com.chirper.infrastructure.security.JwtUtil;
import com.chirper.presentation.dto.tweet.CreateTweetRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TweetControllerTest
 * TweetControllerのテストクラス（TDD RED Phase）
 *
 * テスト対象:
 * - POST /api/v1/tweets (ツイート投稿)
 * - GET /api/v1/tweets/{tweetId} (ツイート取得)
 * - DELETE /api/v1/tweets/{tweetId} (ツイート削除)
 */
@WebMvcTest(controllers = TweetController.class)
@AutoConfigureMockMvc(addFilters = false)
class TweetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTweetUseCase createTweetUseCase;

    @MockBean
    private DeleteTweetUseCase deleteTweetUseCase;

    @MockBean
    private ITweetRepository tweetRepository;

    @MockBean
    private com.chirper.domain.repository.ILikeRepository likeRepository;

    @MockBean
    private com.chirper.domain.repository.IRetweetRepository retweetRepository;

    @MockBean
    private com.chirper.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    private UserId testUserId;
    private TweetId testTweetId;
    private Tweet testTweet;

    @BeforeEach
    void setUp() {
        testUserId = UserId.of(UUID.randomUUID().toString());
        testTweetId = new TweetId(UUID.randomUUID());
        testTweet = Tweet.create(testUserId, new TweetContent("テストツイート"));
        // Use reflection to set the ID and createdAt
        try {
            var idField = Tweet.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testTweet, testTweetId);

            var createdAtField = Tweet.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testTweet, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("POST /api/v1/tweets - ツイート投稿成功（201 Created）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void createTweet_success() throws Exception {
        // Arrange
        CreateTweetRequest request = new CreateTweetRequest("テストツイート");
        when(createTweetUseCase.execute(any(UserId.class), eq("テストツイート")))
            .thenReturn(testTweet);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tweetId").value(testTweetId.value().toString()))
            .andExpect(jsonPath("$.createdAt").exists());

        verify(createTweetUseCase, times(1)).execute(any(UserId.class), eq("テストツイート"));
    }

    @Test
    @DisplayName("POST /api/v1/tweets - ツイート本文が空の場合（400 Bad Request）")
    @WithMockUser
    void createTweet_emptyContent() throws Exception {
        // Arrange
        CreateTweetRequest request = new CreateTweetRequest("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(createTweetUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/tweets - ツイート本文が280文字を超える場合（400 Bad Request）")
    @WithMockUser
    void createTweet_contentTooLong() throws Exception {
        // Arrange
        String longContent = "a".repeat(281);
        CreateTweetRequest request = new CreateTweetRequest(longContent);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(createTweetUseCase, never()).execute(any(), any());
    }

    @Test
    @Disabled("@AutoConfigureMockMvc(addFilters = false)のため、認証テストは統合テストで実装すべき")
    @DisplayName("POST /api/v1/tweets - 認証なしの場合（401 Unauthorized）")
    void createTweet_unauthorized() throws Exception {
        // Arrange
        CreateTweetRequest request = new CreateTweetRequest("テストツイート");

        // Act & Assert
        mockMvc.perform(post("/api/v1/tweets")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());

        verify(createTweetUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("GET /api/v1/tweets/{tweetId} - ツイート取得成功（200 OK）")
    void getTweet_success() throws Exception {
        // Arrange
        when(tweetRepository.findById(testTweetId))
            .thenReturn(Optional.of(testTweet));

        // Act & Assert
        mockMvc.perform(get("/api/v1/tweets/" + testTweetId.value()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tweetId").value(testTweetId.value().toString()))
            .andExpect(jsonPath("$.userId").value(testUserId.value().toString()))
            .andExpect(jsonPath("$.content").value("テストツイート"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.likesCount").exists())
            .andExpect(jsonPath("$.retweetsCount").exists());

        verify(tweetRepository, times(1)).findById(testTweetId);
    }

    @Test
    @DisplayName("GET /api/v1/tweets/{tweetId} - ツイートが存在しない場合（404 Not Found）")
    void getTweet_notFound() throws Exception {
        // Arrange
        TweetId nonExistentId = new TweetId(UUID.randomUUID());
        when(tweetRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/tweets/" + nonExistentId.value()))
            .andExpect(status().isNotFound());

        verify(tweetRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("DELETE /api/v1/tweets/{tweetId} - ツイート削除成功（204 No Content）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void deleteTweet_success() throws Exception {
        // Arrange
        doNothing().when(deleteTweetUseCase).execute(eq(testTweetId), any(UserId.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tweets/" + testTweetId.value())
                .with(csrf()))
            .andExpect(status().isNoContent());

        verify(deleteTweetUseCase, times(1)).execute(eq(testTweetId), any(UserId.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/tweets/{tweetId} - 投稿者以外が削除しようとした場合（403 Forbidden）")
    @WithMockUser(username = "660e8400-e29b-41d4-a716-446655440001")
    void deleteTweet_forbidden() throws Exception {
        // Arrange
        doThrow(new SecurityException("投稿者以外は削除できません"))
            .when(deleteTweetUseCase).execute(eq(testTweetId), any(UserId.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tweets/" + testTweetId.value())
                .with(csrf()))
            .andExpect(status().isForbidden());

        verify(deleteTweetUseCase, times(1)).execute(eq(testTweetId), any(UserId.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/tweets/{tweetId} - ツイートが存在しない場合（404 Not Found）")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void deleteTweet_notFound() throws Exception {
        // Arrange
        TweetId nonExistentId = new TweetId(UUID.randomUUID());
        doThrow(new IllegalArgumentException("Tweet not found"))
            .when(deleteTweetUseCase).execute(eq(nonExistentId), any(UserId.class));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tweets/" + nonExistentId.value())
                .with(csrf()))
            .andExpect(status().isNotFound());

        verify(deleteTweetUseCase, times(1)).execute(eq(nonExistentId), any(UserId.class));
    }

    @Test
    @Disabled("@AutoConfigureMockMvc(addFilters = false)のため、認証テストは統合テストで実装すべき")
    @DisplayName("DELETE /api/v1/tweets/{tweetId} - 認証なしの場合（401 Unauthorized）")
    void deleteTweet_unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/tweets/" + testTweetId.value())
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        verify(deleteTweetUseCase, never()).execute(any(), any());
    }
}
