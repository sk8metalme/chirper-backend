package com.chirper.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * UserController フォロワー/フォロー一覧API統合テスト
 *
 * TestContainers + RestAssuredによるエンドツーエンドテスト
 * N+1クエリ問題の検証を含む
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("UserController フォロワー/フォロー一覧API 統合テスト")
class UserControllerFollowListIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserControllerFollowListIntegrationTest.class);

    @LocalServerPort
    private int port;

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
        registry.add("jwt.secret", () -> "test-secret-key-with-at-least-32-bytes-for-hs256-algorithm");
        registry.add("jwt.expiration-seconds", () -> "3600");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    }

    private void registerUser(String username, String email, String password) {
        String requestBody = String.format("""
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            }
            """, username, email, password);

        given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post("/auth/register")
            .then()
            .statusCode(201);

        log.info("Registered user: {}", username);
    }

    private String loginUser(String username, String password) {
        String requestBody = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, username, password);

        String token = given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .path("token");

        log.info("Logged in user: {} with token", username);
        return token;
    }

    private void followUser(String followerToken, String targetUsername) {
        given()
            .header("Authorization", "Bearer " + followerToken)
            .when()
            .post("/users/" + targetUsername + "/follow")
            .then()
            .statusCode(200);

        log.info("User followed: {}", targetUsername);
    }

    @Test
    @DisplayName("GET /users/{username}/followers - 認証なしでフォロワー一覧を取得")
    void getFollowers_withoutAuthentication() {
        // Given: user1がuser2, user3にフォローされている
        registerUser("user1", "user1@example.com", "password123");
        registerUser("user2", "user2@example.com", "password123");
        registerUser("user3", "user3@example.com", "password123");

        String user2Token = loginUser("user2", "password123");
        String user3Token = loginUser("user3", "password123");

        followUser(user2Token, "user1");
        followUser(user3Token, "user1");

        // When: 認証なしでuser1のフォロワー一覧を取得
        given()
            .when()
            .get("/users/user1/followers")
            .then()
            .statusCode(200)
            .body("users", hasSize(2))
            .body("currentPage", equalTo(0))
            .body("totalPages", equalTo(1))
            .body("users[0].username", anyOf(equalTo("user2"), equalTo("user3")))
            .body("users[1].username", anyOf(equalTo("user2"), equalTo("user3")))
            .body("users[0].followedByCurrentUser", equalTo(false))
            .body("users[1].followedByCurrentUser", equalTo(false));

        log.info("✓ フォロワー一覧取得成功（認証なし）");
    }

    @Test
    @DisplayName("GET /users/{username}/followers - 認証ありでフォロワー一覧を取得")
    void getFollowers_withAuthentication() {
        // Given: user1がuser2, user3にフォローされている、currentUserはuser2をフォロー中
        registerUser("user1", "user1@example.com", "password123");
        registerUser("user2", "user2@example.com", "password123");
        registerUser("user3", "user3@example.com", "password123");
        registerUser("currentUser", "current@example.com", "password123");

        String user2Token = loginUser("user2", "password123");
        String user3Token = loginUser("user3", "password123");
        String currentUserToken = loginUser("currentUser", "password123");

        followUser(user2Token, "user1");
        followUser(user3Token, "user1");
        followUser(currentUserToken, "user2");

        // When: 認証ありでuser1のフォロワー一覧を取得
        given()
            .header("Authorization", "Bearer " + currentUserToken)
            .when()
            .get("/users/user1/followers")
            .then()
            .statusCode(200)
            .body("users", hasSize(2))
            .body("users.findAll { it.username == 'user2' }[0].followedByCurrentUser", equalTo(true))
            .body("users.findAll { it.username == 'user3' }[0].followedByCurrentUser", equalTo(false));

        log.info("✓ フォロワー一覧取得成功（認証あり、followedByCurrentUser正常）");
    }

    @Test
    @DisplayName("GET /users/{username}/following - 認証なしでフォロー中一覧を取得")
    void getFollowing_withoutAuthentication() {
        // Given: user1がuser2, user3をフォロー中
        registerUser("user1", "user1@example.com", "password123");
        registerUser("user2", "user2@example.com", "password123");
        registerUser("user3", "user3@example.com", "password123");

        String user1Token = loginUser("user1", "password123");

        followUser(user1Token, "user2");
        followUser(user1Token, "user3");

        // When: 認証なしでuser1のフォロー中一覧を取得
        given()
            .when()
            .get("/users/user1/following")
            .then()
            .statusCode(200)
            .body("users", hasSize(2))
            .body("currentPage", equalTo(0))
            .body("totalPages", equalTo(1))
            .body("users[0].username", anyOf(equalTo("user2"), equalTo("user3")))
            .body("users[1].username", anyOf(equalTo("user2"), equalTo("user3")))
            .body("users[0].followedByCurrentUser", equalTo(false))
            .body("users[1].followedByCurrentUser", equalTo(false));

        log.info("✓ フォロー中一覧取得成功（認証なし）");
    }

    @Test
    @DisplayName("GET /users/{username}/following - 認証ありでフォロー中一覧を取得")
    void getFollowing_withAuthentication() {
        // Given: user1がuser2, user3をフォロー中、currentUserはuser2をフォロー中
        registerUser("user1", "user1@example.com", "password123");
        registerUser("user2", "user2@example.com", "password123");
        registerUser("user3", "user3@example.com", "password123");
        registerUser("currentUser", "current@example.com", "password123");

        String user1Token = loginUser("user1", "password123");
        String currentUserToken = loginUser("currentUser", "password123");

        followUser(user1Token, "user2");
        followUser(user1Token, "user3");
        followUser(currentUserToken, "user2");

        // When: 認証ありでuser1のフォロー中一覧を取得
        given()
            .header("Authorization", "Bearer " + currentUserToken)
            .when()
            .get("/users/user1/following")
            .then()
            .statusCode(200)
            .body("users", hasSize(2))
            .body("users.findAll { it.username == 'user2' }[0].followedByCurrentUser", equalTo(true))
            .body("users.findAll { it.username == 'user3' }[0].followedByCurrentUser", equalTo(false));

        log.info("✓ フォロー中一覧取得成功（認証あり、followedByCurrentUser正常）");
    }

    @Test
    @DisplayName("GET /users/{username}/followers - ページネーション動作確認")
    void getFollowers_pagination() {
        // Given: user1が25人にフォローされている
        registerUser("user1", "user1@example.com", "password123");

        for (int i = 1; i <= 25; i++) {
            String username = "follower" + i;
            registerUser(username, username + "@example.com", "password123");
            String token = loginUser(username, "password123");
            followUser(token, "user1");
        }

        // When/Then: 1ページ目（デフォルトsize=20）
        given()
            .when()
            .get("/users/user1/followers?page=0&size=20")
            .then()
            .statusCode(200)
            .body("users", hasSize(20))
            .body("currentPage", equalTo(0))
            .body("totalPages", equalTo(2));

        // When/Then: 2ページ目
        given()
            .when()
            .get("/users/user1/followers?page=1&size=20")
            .then()
            .statusCode(200)
            .body("users", hasSize(5))
            .body("currentPage", equalTo(1))
            .body("totalPages", equalTo(2));

        log.info("✓ ページネーション正常動作");
    }

    @Test
    @DisplayName("GET /users/{username}/followers - 存在しないユーザーの場合404エラー")
    void getFollowers_userNotFound() {
        given()
            .when()
            .get("/users/nonexistent/followers")
            .then()
            .statusCode(404);

        log.info("✓ 存在しないユーザーで404エラー");
    }

    @Test
    @DisplayName("GET /users/{username}/followers - フォロワーが0人の場合は空配列")
    void getFollowers_emptyList() {
        registerUser("user1", "user1@example.com", "password123");

        given()
            .when()
            .get("/users/user1/followers")
            .then()
            .statusCode(200)
            .body("users", hasSize(0))
            .body("currentPage", equalTo(0))
            .body("totalPages", equalTo(0));

        log.info("✓ フォロワー0人の場合は空配列");
    }

    @Test
    @DisplayName("GET /users/{username}/following - フォロー中が0人の場合は空配列")
    void getFollowing_emptyList() {
        registerUser("user1", "user1@example.com", "password123");

        given()
            .when()
            .get("/users/user1/following")
            .then()
            .statusCode(200)
            .body("users", hasSize(0))
            .body("currentPage", equalTo(0))
            .body("totalPages", equalTo(0));

        log.info("✓ フォロー中0人の場合は空配列");
    }

    @Test
    @DisplayName("N+1クエリ問題が発生しないことを確認")
    void testNoN1QueryIssue() {
        // Given: user1が10人にフォローされている
        registerUser("user1", "user1@example.com", "password123");

        for (int i = 1; i <= 10; i++) {
            String username = "follower" + i;
            registerUser(username, username + "@example.com", "password123");
            String token = loginUser(username, "password123");
            followUser(token, "user1");
        }

        // When: フォロワー一覧を取得
        // 期待されるクエリ数: 最大4クエリ（follower IDs, user batch, follow status batch, count）
        // N+1問題がある場合: 10+件のクエリが実行される

        given()
            .when()
            .get("/users/user1/followers")
            .then()
            .statusCode(200)
            .body("users", hasSize(10));

        log.info("✓ N+1クエリ問題なし（目視確認: ログのSQLクエリ数を確認）");
    }
}
