package com.chirper.infrastructure.persistence.repository;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.Password;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository統合テスト（TestContainers + JUnit 5）
 *
 * UserRepositoryImplのデータアクセステスト
 * TestContainersでPostgreSQLを起動し、実際のデータベースでテスト
 */
@SpringBootTest
@Testcontainers
@DisplayName("UserRepository 統合テスト")
class UserRepositoryIntegrationTest {

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

    @BeforeEach
    void setUp() {
        // テスト間のデータクリーンアップ（テスト独立性確保）
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("save() - ユーザーを保存できること")
    void save_shouldPersistUser() {
        // Given
        Username username = new Username("testuser");
        Email email = new Email("test@example.com");
        User user = User.create(username, email, "password123");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals(username, savedUser.getUsername());
        assertEquals(email, savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @DisplayName("findById() - IDでユーザーを取得できること")
    void findById_shouldReturnUser_whenUserExists() {
        // Given
        User user = createAndSaveUser("finduser", "find@example.com");
        UserId userId = user.getId();

        // When
        Optional<User> foundUser = userRepository.findById(userId);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("finduser", foundUser.get().getUsername().value());
    }

    @Test
    @DisplayName("findById() - 存在しないIDの場合は空を返すこと")
    void findById_shouldReturnEmpty_whenUserDoesNotExist() {
        // Given
        UserId nonExistentId = new UserId(UUID.randomUUID());

        // When
        Optional<User> foundUser = userRepository.findById(nonExistentId);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("findByUsername() - ユーザー名でユーザーを取得できること")
    void findByUsername_shouldReturnUser_whenUsernameExists() {
        // Given
        createAndSaveUser("uniqueuser", "unique@example.com");
        Username username = new Username("uniqueuser");

        // When
        Optional<User> foundUser = userRepository.findByUsername(username);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("uniqueuser", foundUser.get().getUsername().value());
    }

    @Test
    @DisplayName("findByUsername() - 存在しないユーザー名の場合は空を返すこと")
    void findByUsername_shouldReturnEmpty_whenUsernameDoesNotExist() {
        // Given
        Username nonExistentUsername = new Username("nonexistent");

        // When
        Optional<User> foundUser = userRepository.findByUsername(nonExistentUsername);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("findByEmail() - メールアドレスでユーザーを取得できること")
    void findByEmail_shouldReturnUser_whenEmailExists() {
        // Given
        createAndSaveUser("emailuser", "emailuser@example.com");
        Email email = new Email("emailuser@example.com");

        // When
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("emailuser@example.com", foundUser.get().getEmail().value());
    }

    @Test
    @DisplayName("findByEmail() - 存在しないメールアドレスの場合は空を返すこと")
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        // Given
        Email nonExistentEmail = new Email("nonexistent@example.com");

        // When
        Optional<User> foundUser = userRepository.findByEmail(nonExistentEmail);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("delete() - ユーザーを削除できること")
    void delete_shouldRemoveUser() {
        // Given
        User user = createAndSaveUser("deleteuser", "delete@example.com");
        UserId userId = user.getId();

        // When
        userRepository.delete(userId);

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @DisplayName("searchByKeyword() - キーワードでユーザーを検索できること（username部分一致）")
    void searchByKeyword_shouldFindUsers_byUsername() {
        // Given
        createAndSaveUser("john_doe", "john@example.com");
        createAndSaveUser("john_smith", "johnsmith@example.com");
        createAndSaveUser("alice", "alice@example.com");

        // When
        List<User> results = userRepository.searchByKeyword("john", 0, 10);

        // Then
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().value().equals("john_doe")));
        assertTrue(results.stream().anyMatch(u -> u.getUsername().value().equals("john_smith")));
    }

    @Test
    @DisplayName("searchByKeyword() - キーワードでユーザーを検索できること（displayName部分一致）")
    void searchByKeyword_shouldFindUsers_byDisplayName() {
        // Given
        User user1 = User.create(new Username("user1"), new Email("user1@example.com"), "password123");
        user1.updateProfile("John Display", "bio", null);
        userRepository.save(user1);

        User user2 = User.create(new Username("user2"), new Email("user2@example.com"), "password123");
        user2.updateProfile("Alice", "bio", null);
        userRepository.save(user2);

        // When
        List<User> results = userRepository.searchByKeyword("Display", 0, 10);

        // Then
        assertEquals(1, results.size());
        assertEquals("user1", results.get(0).getUsername().value());
    }

    @Test
    @DisplayName("searchByKeyword() - ページネーション処理が正しく動作すること")
    void searchByKeyword_shouldSupportPagination() {
        // Given
        for (int i = 1; i <= 25; i++) {
            createAndSaveUser("user" + i, "user" + i + "@example.com");
        }

        // When - 1ページ目（10件）
        List<User> page1 = userRepository.searchByKeyword("user", 0, 10);
        // When - 2ページ目（10件）
        List<User> page2 = userRepository.searchByKeyword("user", 1, 10);
        // When - 3ページ目（5件）
        List<User> page3 = userRepository.searchByKeyword("user", 2, 10);

        // Then
        assertEquals(10, page1.size());
        assertEquals(10, page2.size());
        assertEquals(5, page3.size());
    }

    @Test
    @DisplayName("countByKeyword() - キーワード検索結果数を正しくカウントできること")
    void countByKeyword_shouldReturnCorrectCount() {
        // Given
        createAndSaveUser("john_doe", "john@example.com");
        createAndSaveUser("john_smith", "johnsmith@example.com");
        createAndSaveUser("alice", "alice@example.com");

        // When
        long count = userRepository.countByKeyword("john");

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("findByIds() - 複数IDでユーザーをバッチ取得できること")
    void findByIds_shouldReturnMultipleUsers() {
        // Given
        User user1 = createAndSaveUser("user1", "user1@example.com");
        User user2 = createAndSaveUser("user2", "user2@example.com");
        User user3 = createAndSaveUser("user3", "user3@example.com");

        List<UserId> userIds = List.of(user1.getId(), user2.getId(), user3.getId());

        // When
        Map<UserId, User> usersMap = userRepository.findByIds(userIds);

        // Then
        assertEquals(3, usersMap.size());
        assertTrue(usersMap.containsKey(user1.getId()));
        assertTrue(usersMap.containsKey(user2.getId()));
        assertTrue(usersMap.containsKey(user3.getId()));
    }

    @Test
    @DisplayName("findByIds() - 空リストの場合は空マップを返すこと")
    void findByIds_shouldReturnEmptyMap_whenEmptyList() {
        // When
        Map<UserId, User> usersMap = userRepository.findByIds(List.of());

        // Then
        assertTrue(usersMap.isEmpty());
    }

    @Test
    @DisplayName("findByIds() - 存在しないIDは結果に含まれないこと")
    void findByIds_shouldNotIncludeNonExistentIds() {
        // Given
        User user1 = createAndSaveUser("user1", "user1@example.com");
        UserId nonExistentId = new UserId(UUID.randomUUID());

        List<UserId> userIds = List.of(user1.getId(), nonExistentId);

        // When
        Map<UserId, User> usersMap = userRepository.findByIds(userIds);

        // Then
        assertEquals(1, usersMap.size());
        assertTrue(usersMap.containsKey(user1.getId()));
        assertFalse(usersMap.containsKey(nonExistentId));
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
}
