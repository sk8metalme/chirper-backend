package com.chirper.presentation.controller;

import com.chirper.application.usecase.SearchUseCase;
import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.*;
import com.chirper.infrastructure.security.JwtUtil;
import com.chirper.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SearchControllerTest
 * SearchControllerのテストクラス（TDD RED Phase）
 *
 * テスト対象:
 * - GET /api/v1/search (ユーザー・ツイート検索)
 */
@WebMvcTest(controllers = SearchController.class)
@AutoConfigureMockMvc(addFilters = false)

class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchUseCase searchUseCase;

    @MockBean
    private com.chirper.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    private User testUser;
    private Tweet testTweet;

    @BeforeEach
    void setUp() {
        UserId userId = UserId.of(UUID.randomUUID().toString());
        testUser = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            "password123"
        );

        // リフレクションでIDを設定
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testTweet = Tweet.create(userId, new TweetContent("テストツイート"));
        try {
            var tweetIdField = Tweet.class.getDeclaredField("id");
            tweetIdField.setAccessible(true);
            tweetIdField.set(testTweet, new TweetId(UUID.randomUUID()));

            var createdAtField = Tweet.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testTweet, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("GET /api/v1/search - 検索成功（200 OK）")
    void search_success() throws Exception {
        // Arrange
        var searchResult = new SearchUseCase.SearchResult(
            List.of(testUser),
            List.of(testTweet)
        );
        when(searchUseCase.execute(eq("test"), eq(0), eq(20)))
            .thenReturn(searchResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/search")
                .param("query", "test")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray())
            .andExpect(jsonPath("$.users.length()").value(1))
            .andExpect(jsonPath("$.users[0].username").value("testuser"))
            .andExpect(jsonPath("$.tweets").isArray())
            .andExpect(jsonPath("$.tweets.length()").value(1))
            .andExpect(jsonPath("$.tweets[0].content").value("テストツイート"));

        verify(searchUseCase, times(1)).execute(eq("test"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/search - デフォルトページネーション（page=0, size=20）")
    void search_defaultPagination() throws Exception {
        // Arrange
        var searchResult = new SearchUseCase.SearchResult(List.of(), List.of());
        when(searchUseCase.execute(eq("test"), eq(0), eq(20)))
            .thenReturn(searchResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/search")
                .param("query", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray())
            .andExpect(jsonPath("$.users.length()").value(0))
            .andExpect(jsonPath("$.tweets").isArray())
            .andExpect(jsonPath("$.tweets.length()").value(0));

        verify(searchUseCase, times(1)).execute(eq("test"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/search - キーワードが2文字未満の場合（400 Bad Request）")
    void search_queryTooShort() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/search")
                .param("query", "a")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isBadRequest());

        verify(searchUseCase, never()).execute(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/search - クエリパラメータが空の場合（400 Bad Request）")
    void search_emptyQuery() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/search")
                .param("query", ""))
            .andExpect(status().isBadRequest());

        verify(searchUseCase, never()).execute(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/search - クエリパラメータがない場合（400 Bad Request）")
    void search_missingQuery() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/search"))
            .andExpect(status().isBadRequest());

        verify(searchUseCase, never()).execute(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/search - カスタムページネーション（page=1, size=10）")
    void search_customPagination() throws Exception {
        // Arrange
        var searchResult = new SearchUseCase.SearchResult(
            List.of(testUser),
            List.of(testTweet)
        );
        when(searchUseCase.execute(eq("test"), eq(1), eq(10)))
            .thenReturn(searchResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/search")
                .param("query", "test")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray())
            .andExpect(jsonPath("$.users.length()").value(1))
            .andExpect(jsonPath("$.tweets").isArray())
            .andExpect(jsonPath("$.tweets.length()").value(1));

        verify(searchUseCase, times(1)).execute(eq("test"), eq(1), eq(10));
    }
}
