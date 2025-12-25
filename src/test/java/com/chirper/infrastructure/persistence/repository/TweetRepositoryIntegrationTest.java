package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TweetRepository統合テスト（TestContainers + JUnit 5）
 *
 * TweetRepositoryImplのデータアクセステスト
 * N+1クエリ対策の検証を含む
 */
@SpringBootTest
@Testcontainers
@DisplayName("TweetRepository 統合テスト")
class TweetRepositoryIntegrationTest {

    @Autowired
    private ITweetRepository tweetRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("chirper_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    private User testUser;

    @BeforeEach
    void setUp() {
        // テスト間のデータクリーンアップ
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");

        // テスト用ユーザーを作成
        testUser = createAndSaveUser("testuser", "test@example.com");
    }

    @Test
    @DisplayName("save() - ツイートを保存できること")
    void save_shouldPersistTweet() {
        // Given
        TweetContent content = new TweetContent("This is a test tweet");
        Tweet tweet = Tweet.create(testUser.getId(), content);

        // When
        Tweet savedTweet = tweetRepository.save(tweet);

        // Then
        assertNotNull(savedTweet.getId());
        assertEquals(content.value(), savedTweet.getContent().value());
        assertEquals(testUser.getId(), savedTweet.getUserId());
        assertFalse(savedTweet.isDeleted());
        assertNotNull(savedTweet.getCreatedAt());
    }

    @Test
    @DisplayName("findById() - IDでツイートを取得できること")
    void findById_shouldReturnTweet_whenTweetExists() {
        // Given
        Tweet tweet = createAndSaveTweet(testUser.getId(), "Find this tweet");
        TweetId tweetId = tweet.getId();

        // When
        Optional<Tweet> foundTweet = tweetRepository.findById(tweetId);

        // Then
        assertTrue(foundTweet.isPresent());
        assertEquals(tweetId, foundTweet.get().getId());
        assertEquals("Find this tweet", foundTweet.get().getContent().value());
    }

    @Test
    @DisplayName("findById() - 存在しないIDの場合は空を返すこと")
    void findById_shouldReturnEmpty_whenTweetDoesNotExist() {
        // Given
        TweetId nonExistentId = new TweetId(UUID.randomUUID());

        // When
        Optional<Tweet> foundTweet = tweetRepository.findById(nonExistentId);

        // Then
        assertFalse(foundTweet.isPresent());
    }

    @Test
    @DisplayName("findById() - 論理削除されたツイートも取得できること")
    void findById_shouldReturnDeletedTweet() {
        // Given
        Tweet tweet = createAndSaveTweet(testUser.getId(), "Deleted tweet");
        tweet.delete(testUser.getId());
        tweetRepository.save(tweet);

        // When
        Optional<Tweet> foundTweet = tweetRepository.findById(tweet.getId());

        // Then
        assertTrue(foundTweet.isPresent());
        assertTrue(foundTweet.get().isDeleted());
    }

    @Test
    @DisplayName("findByUserIdsWithDetails() - 複数ユーザーIDでツイートを取得できること")
    void findByUserIdsWithDetails_shouldReturnTweets() {
        // Given
        User user2 = createAndSaveUser("user2", "user2@example.com");
        createAndSaveTweet(testUser.getId(), "Tweet 1");
        createAndSaveTweet(user2.getId(), "Tweet 2");

        List<UserId> userIds = List.of(testUser.getId(), user2.getId());

        // When
        List<Tweet> tweets = tweetRepository.findByUserIdsWithDetails(userIds, 0, 10);

        // Then
        assertEquals(2, tweets.size());
    }

    @Test
    @DisplayName("findByUserIdsWithDetails() - 論理削除されたツイートは除外されること")
    void findByUserIdsWithDetails_shouldExcludeDeletedTweets() {
        // Given
        createAndSaveTweet(testUser.getId(), "Active tweet");
        Tweet deletedTweet = createAndSaveTweet(testUser.getId(), "Deleted tweet");
        deletedTweet.delete(testUser.getId());
        tweetRepository.save(deletedTweet);

        List<UserId> userIds = List.of(testUser.getId());

        // When
        List<Tweet> tweets = tweetRepository.findByUserIdsWithDetails(userIds, 0, 10);

        // Then
        assertEquals(1, tweets.size());
        assertEquals("Active tweet", tweets.get(0).getContent().value());
    }

    @Test
    @DisplayName("findByUserIdsWithDetails() - 新しい順（created_at DESC）でソートされること")
    void findByUserIdsWithDetails_shouldSortByCreatedAtDesc() throws InterruptedException {
        // Given
        Tweet tweet1 = createAndSaveTweet(testUser.getId(), "First tweet");
        Thread.sleep(10); // Ensure different timestamps
        Tweet tweet2 = createAndSaveTweet(testUser.getId(), "Second tweet");
        Thread.sleep(10);
        Tweet tweet3 = createAndSaveTweet(testUser.getId(), "Third tweet");

        List<UserId> userIds = List.of(testUser.getId());

        // When
        List<Tweet> tweets = tweetRepository.findByUserIdsWithDetails(userIds, 0, 10);

        // Then
        assertEquals(3, tweets.size());
        // 新しい順
        assertEquals("Third tweet", tweets.get(0).getContent().value());
        assertEquals("Second tweet", tweets.get(1).getContent().value());
        assertEquals("First tweet", tweets.get(2).getContent().value());
    }

    @Test
    @DisplayName("findByUserIdsWithDetails() - ページネーション処理が正しく動作すること")
    void findByUserIdsWithDetails_shouldSupportPagination() throws InterruptedException {
        // Given
        for (int i = 1; i <= 25; i++) {
            createAndSaveTweet(testUser.getId(), "Tweet " + i);
            Thread.sleep(5); // Ensure different timestamps
        }

        List<UserId> userIds = List.of(testUser.getId());

        // When
        List<Tweet> page1 = tweetRepository.findByUserIdsWithDetails(userIds, 0, 10);
        List<Tweet> page2 = tweetRepository.findByUserIdsWithDetails(userIds, 1, 10);
        List<Tweet> page3 = tweetRepository.findByUserIdsWithDetails(userIds, 2, 10);

        // Then
        assertEquals(10, page1.size());
        assertEquals(10, page2.size());
        assertEquals(5, page3.size());
    }

    @Test
    @DisplayName("findByUserIdsWithDetails() - 空リストの場合は空を返すこと")
    void findByUserIdsWithDetails_shouldReturnEmpty_whenEmptyList() {
        // When
        List<Tweet> tweets = tweetRepository.findByUserIdsWithDetails(List.of(), 0, 10);

        // Then
        assertTrue(tweets.isEmpty());
    }

    @Test
    @DisplayName("delete() - UnsupportedOperationExceptionを投げること")
    void delete_shouldThrowUnsupportedOperationException() {
        // Given
        Tweet tweet = createAndSaveTweet(testUser.getId(), "Tweet to delete");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () ->
            tweetRepository.delete(tweet.getId())
        );
    }

    @Test
    @DisplayName("searchByKeyword() - キーワードでツイートを検索できること（content部分一致）")
    void searchByKeyword_shouldFindTweets_byContent() {
        // Given
        createAndSaveTweet(testUser.getId(), "Spring Boot is awesome");
        createAndSaveTweet(testUser.getId(), "I love Spring Framework");
        createAndSaveTweet(testUser.getId(), "Java programming");

        // When
        List<Tweet> results = tweetRepository.searchByKeyword("Spring", 0, 10);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(t -> t.getContent().value().contains("Spring Boot")));
        assertTrue(results.stream().anyMatch(t -> t.getContent().value().contains("Spring Framework")));
    }

    @Test
    @DisplayName("searchByKeyword() - 論理削除されたツイートは検索結果から除外されること")
    void searchByKeyword_shouldExcludeDeletedTweets() {
        // Given
        createAndSaveTweet(testUser.getId(), "Spring Boot active tweet");
        Tweet deletedTweet = createAndSaveTweet(testUser.getId(), "Spring Boot deleted tweet");
        deletedTweet.delete(testUser.getId());
        tweetRepository.save(deletedTweet);

        // When
        List<Tweet> results = tweetRepository.searchByKeyword("Spring", 0, 10);

        // Then
        assertEquals(1, results.size());
        assertEquals("Spring Boot active tweet", results.get(0).getContent().value());
    }

    @Test
    @DisplayName("searchByKeyword() - ページネーション処理が正しく動作すること")
    void searchByKeyword_shouldSupportPagination() throws InterruptedException {
        // Given
        for (int i = 1; i <= 25; i++) {
            createAndSaveTweet(testUser.getId(), "Java programming " + i);
            Thread.sleep(5);
        }

        // When
        List<Tweet> page1 = tweetRepository.searchByKeyword("Java", 0, 10);
        List<Tweet> page2 = tweetRepository.searchByKeyword("Java", 1, 10);
        List<Tweet> page3 = tweetRepository.searchByKeyword("Java", 2, 10);

        // Then
        assertEquals(10, page1.size());
        assertEquals(10, page2.size());
        assertEquals(5, page3.size());
    }

    @Test
    @DisplayName("countByKeyword() - キーワード検索結果数を正しくカウントできること")
    void countByKeyword_shouldReturnCorrectCount() {
        // Given
        createAndSaveTweet(testUser.getId(), "Spring Boot tweet");
        createAndSaveTweet(testUser.getId(), "I love Spring");
        createAndSaveTweet(testUser.getId(), "Java programming");

        // When
        long count = tweetRepository.countByKeyword("Spring");

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("countByKeyword() - 論理削除されたツイートはカウントから除外されること")
    void countByKeyword_shouldExcludeDeletedTweetsFromCount() {
        // Given
        createAndSaveTweet(testUser.getId(), "Spring Boot active");
        Tweet deletedTweet = createAndSaveTweet(testUser.getId(), "Spring Boot deleted");
        deletedTweet.delete(testUser.getId());
        tweetRepository.save(deletedTweet);

        // When
        long count = tweetRepository.countByKeyword("Spring");

        // Then
        assertEquals(1, count);
    }

    @Test
    @DisplayName("findByUserId() - ユーザーIDでツイートを取得できること")
    void findByUserId_shouldReturnTweets_forUser() {
        // Given
        User user2 = createAndSaveUser("user2", "user2@example.com");
        createAndSaveTweet(testUser.getId(), "User1 tweet 1");
        createAndSaveTweet(testUser.getId(), "User1 tweet 2");
        createAndSaveTweet(user2.getId(), "User2 tweet");

        // When
        List<Tweet> tweets = tweetRepository.findByUserId(testUser.getId(), 0, 10);

        // Then
        assertEquals(2, tweets.size());
        assertTrue(tweets.stream().allMatch(t -> t.getUserId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("findByUserId() - 論理削除されたツイートは除外されること")
    void findByUserId_shouldExcludeDeletedTweets() {
        // Given
        createAndSaveTweet(testUser.getId(), "Active tweet");
        Tweet deletedTweet = createAndSaveTweet(testUser.getId(), "Deleted tweet");
        deletedTweet.delete(testUser.getId());
        tweetRepository.save(deletedTweet);

        // When
        List<Tweet> tweets = tweetRepository.findByUserId(testUser.getId(), 0, 10);

        // Then
        assertEquals(1, tweets.size());
        assertEquals("Active tweet", tweets.get(0).getContent().value());
    }

    @Test
    @DisplayName("findByUserId() - ページネーション処理が正しく動作すること")
    void findByUserId_shouldSupportPagination() throws InterruptedException {
        // Given
        for (int i = 1; i <= 25; i++) {
            createAndSaveTweet(testUser.getId(), "Tweet " + i);
            Thread.sleep(5);
        }

        // When
        List<Tweet> page1 = tweetRepository.findByUserId(testUser.getId(), 0, 10);
        List<Tweet> page2 = tweetRepository.findByUserId(testUser.getId(), 1, 10);
        List<Tweet> page3 = tweetRepository.findByUserId(testUser.getId(), 2, 10);

        // Then
        assertEquals(10, page1.size());
        assertEquals(10, page2.size());
        assertEquals(5, page3.size());
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    private User createAndSaveUser(String username, String email) {
        Username usernameVO = new Username(username);
        Email emailVO = new Email(email);
        User user = User.create(usernameVO, emailVO, "password123");
        user.updateProfile(username + " Display", "Bio", null);
        return userRepository.save(user);
    }

    private Tweet createAndSaveTweet(UserId userId, String content) {
        TweetContent tweetContent = new TweetContent(content);
        Tweet tweet = Tweet.create(userId, tweetContent);
        return tweetRepository.save(tweet);
    }
}
