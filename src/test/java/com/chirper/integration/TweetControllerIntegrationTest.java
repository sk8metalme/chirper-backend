package com.chirper.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * TweetController統合テスト（TestContainers + RestAssured）
 *
 * ツイート投稿・取得・削除APIのエンドツーエンドテスト
 * JWT認証フローを含むテスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("TweetController 統合テスト")
class TweetControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private com.chirper.presentation.controller.TweetController tweetController;

    @Autowired(required = false)
    private com.chirper.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

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
        // JWT設定（テスト用）
        registry.add("jwt.secret", () -> "test-secret-key-with-at-least-32-bytes-for-hs256-algorithm");
        registry.add("jwt.expiration-seconds", () -> "3600");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";

        // Bean登録の確認
        if (tweetController == null) {
            throw new RuntimeException("TweetController is not registered as a Bean!");
        }
        if (jwtAuthenticationFilter == null) {
            throw new RuntimeException("JwtAuthenticationFilter is not registered as a Bean!");
        }
        System.out.println("✓ TweetController Bean registered");
        System.out.println("✓ JwtAuthenticationFilter Bean registered");

        // テスト間のデータクリーンアップ（テスト独立性確保）
        cleanupDatabase();
    }

    /**
     * テスト用データベースのクリーンアップ
     * 各テスト実行前にすべてのテーブルをクリアし、IDシーケンスをリセット
     */
    private void cleanupDatabase() {
        // CASCADE削除で関連データも削除される
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    }

    /**
     * ヘルパーメソッド: ユーザー登録
     */
    private void registerUser(String username, String email, String password) {
        String requestBody = """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(username, email, password);

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201);
    }

    /**
     * ヘルパーメソッド: ユーザーログインしてJWTトークンを取得
     */
    private String loginAndGetToken(String username, String password) {
        String loginBody = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(username, password);

        return given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    }

    // ========================================
    // ツイート投稿テスト
    // ========================================

    @Test
    @DisplayName("POST /tweets - 有効なリクエストでツイート投稿が成功する（201 Created）")
    void createTweet_withValidRequest_shouldReturn201() {
        // ユーザー登録とログイン
        registerUser("tweetuser", "tweet@example.com", "password123");
        String token = loginAndGetToken("tweetuser", "password123");

        System.out.println("JWT Token: " + token);

        String requestBody = """
            {
                "content": "これは最初のツイートです"
            }
            """;

        io.restassured.response.Response response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(requestBody)
            .log().all() // リクエスト詳細をログ出力
        .when()
            .post("http://localhost:" + port + "/api/v1/tweets")
        .then()
            .log().all() // レスポンス詳細をログ出力
            .extract()
            .response();

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        response.then()
            .statusCode(201)
            .body("tweetId", notNullValue())
            .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("POST /tweets - JWT認証なしの場合401を返す")
    void createTweet_withoutAuthentication_shouldReturn401() {
        String requestBody = """
            {
                "content": "認証なしでツイート投稿を試みる"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("POST /tweets - ツイート本文が280文字を超える場合400を返す")
    void createTweet_withContentExceeding280Characters_shouldReturn400() {
        // ユーザー登録とログイン
        registerUser("longuser", "long@example.com", "password123");
        String token = loginAndGetToken("longuser", "password123");

        // 281文字のツイート
        String longContent = "あ".repeat(281);
        String requestBody = """
            {
                "content": "%s"
            }
            """.formatted(longContent);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(requestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /tweets - ツイート本文が空の場合400を返す")
    void createTweet_withEmptyContent_shouldReturn400() {
        // ユーザー登録とログイン
        registerUser("emptyuser", "empty@example.com", "password123");
        String token = loginAndGetToken("emptyuser", "password123");

        String requestBody = """
            {
                "content": ""
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(requestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /tweets - 280文字ちょうどのツイートは成功する")
    void createTweet_withExactly280Characters_shouldReturn201() {
        // ユーザー登録とログイン
        registerUser("exactuser", "exact@example.com", "password123");
        String token = loginAndGetToken("exactuser", "password123");

        // 280文字のツイート
        String exactContent = "あ".repeat(280);
        String requestBody = """
            {
                "content": "%s"
            }
            """.formatted(exactContent);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(requestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .body("tweetId", notNullValue());
    }

    // ========================================
    // ツイート取得テスト
    // ========================================

    @Test
    @DisplayName("GET /tweets/{tweetId} - 存在するツイートを取得できる（200 OK）")
    void getTweet_withExistingTweetId_shouldReturn200() {
        // ユーザー登録とログイン
        registerUser("getuser", "get@example.com", "password123");
        String token = loginAndGetToken("getuser", "password123");

        // ツイート投稿
        String createRequestBody = """
            {
                "content": "取得テスト用のツイート"
            }
            """;

        String tweetId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(createRequestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .extract()
            .path("tweetId");

        // ツイート取得
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tweets/" + tweetId)
        .then()
            .statusCode(200)
            .body("tweetId", equalTo(tweetId))
            .body("content", equalTo("取得テスト用のツイート"))
            .body("createdAt", notNullValue())
            .body("likesCount", equalTo(0))
            .body("retweetsCount", equalTo(0));
    }

    @Test
    @DisplayName("GET /tweets/{tweetId} - 存在しないツイートIDの場合404を返す")
    void getTweet_withNonExistentTweetId_shouldReturn404() {
        // ユーザー登録とログイン
        registerUser("notfounduser", "notfound@example.com", "password123");
        String token = loginAndGetToken("notfounduser", "password123");

        String nonExistentTweetId = "00000000-0000-0000-0000-000000000000";

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tweets/" + nonExistentTweetId)
        .then()
            .statusCode(404)
            .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /tweets/{tweetId} - 論理削除されたツイートは取得できない（404を返す）")
    void getTweet_withDeletedTweet_shouldReturn404() {
        // ユーザー登録とログイン
        registerUser("deleteuser", "delete@example.com", "password123");
        String token = loginAndGetToken("deleteuser", "password123");

        // ツイート投稿
        String createRequestBody = """
            {
                "content": "削除されるツイート"
            }
            """;

        String tweetId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(createRequestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .extract()
            .path("tweetId");

        // ツイート削除
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tweets/" + tweetId)
        .then()
            .statusCode(204);

        // 削除されたツイートを取得しようとする → 404
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/tweets/" + tweetId)
        .then()
            .statusCode(404)
            .body("code", equalTo("NOT_FOUND"));
    }

    // ========================================
    // ツイート削除テスト
    // ========================================

    @Test
    @DisplayName("DELETE /tweets/{tweetId} - 投稿者本人が削除できる（204 No Content）")
    void deleteTweet_byOwner_shouldReturn204() {
        // ユーザー登録とログイン
        registerUser("owneruser", "owner@example.com", "password123");
        String token = loginAndGetToken("owneruser", "password123");

        // ツイート投稿
        String createRequestBody = """
            {
                "content": "削除テスト用のツイート"
            }
            """;

        String tweetId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(createRequestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .extract()
            .path("tweetId");

        // ツイート削除
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tweets/" + tweetId)
        .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("DELETE /tweets/{tweetId} - JWT認証なしの場合401を返す")
    void deleteTweet_withoutAuthentication_shouldReturn401() {
        // ユーザー登録とログイン
        registerUser("authuser", "auth@example.com", "password123");
        String token = loginAndGetToken("authuser", "password123");

        // ツイート投稿
        String createRequestBody = """
            {
                "content": "認証テスト用のツイート"
            }
            """;

        String tweetId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(createRequestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .extract()
            .path("tweetId");

        // 認証なしで削除を試みる → 401
        given()
        .when()
            .delete("/tweets/" + tweetId)
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("DELETE /tweets/{tweetId} - 他人のツイートは削除できない（403 Forbidden）")
    void deleteTweet_byOtherUser_shouldReturn403() {
        // ユーザー1: ツイート投稿者
        registerUser("user1", "user1@example.com", "password123");
        String user1Token = loginAndGetToken("user1", "password123");

        // ツイート投稿
        String createRequestBody = """
            {
                "content": "user1のツイート"
            }
            """;

        String tweetId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + user1Token)
            .body(createRequestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .extract()
            .path("tweetId");

        // ユーザー2: 他人
        registerUser("user2", "user2@example.com", "password123");
        String user2Token = loginAndGetToken("user2", "password123");

        // ユーザー2が他人のツイートを削除しようとする → 403
        given()
            .header("Authorization", "Bearer " + user2Token)
        .when()
            .delete("/tweets/" + tweetId)
        .then()
            .statusCode(403)
            .body("code", equalTo("FORBIDDEN"));
    }

    @Test
    @DisplayName("DELETE /tweets/{tweetId} - 存在しないツイートIDの場合404を返す")
    void deleteTweet_withNonExistentTweetId_shouldReturn404() {
        // ユーザー登録とログイン
        registerUser("notfounduser", "notfound@example.com", "password123");
        String token = loginAndGetToken("notfounduser", "password123");

        String nonExistentTweetId = "00000000-0000-0000-0000-000000000000";

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tweets/" + nonExistentTweetId)
        .then()
            .statusCode(404)
            .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /tweets/{tweetId} - 既に削除されたツイートは再度削除できない（404を返す）")
    void deleteTweet_alreadyDeleted_shouldReturn404() {
        // ユーザー登録とログイン
        registerUser("redeletuser", "redelet@example.com", "password123");
        String token = loginAndGetToken("redeletuser", "password123");

        // ツイート投稿
        String createRequestBody = """
            {
                "content": "再削除テスト用のツイート"
            }
            """;

        String tweetId = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(createRequestBody)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .extract()
            .path("tweetId");

        // ツイート削除
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tweets/" + tweetId)
        .then()
            .statusCode(204);

        // 既に削除されたツイートを再度削除しようとする → 404
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/tweets/" + tweetId)
        .then()
            .statusCode(404)
            .body("code", equalTo("NOT_FOUND"));
    }
}
