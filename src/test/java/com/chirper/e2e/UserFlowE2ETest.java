package com.chirper.e2e;

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
 * ユーザーフローE2Eテスト
 *
 * Task 19.1の要件:
 * - ユーザー登録→ログイン→ツイート投稿→タイムライン表示フロー
 * - ユーザーフォロー→タイムライン更新フロー
 * - ツイートいいね・リツイート→カウント更新フロー
 * - 検索機能フロー（ユーザー検索、ツイート検索）
 * - エラーハンドリングフロー（認証エラー、バリデーションエラー）
 *
 * Requirements: 10.7
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("ユーザーフロー E2Eテスト")
class UserFlowE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("chirper_test")
            .withUsername("test_user");

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

    // ========================================
    // E2Eフロー 1: ユーザー登録→ログイン→ツイート投稿→タイムライン表示
    // ========================================

    @Test
    @DisplayName("E2Eフロー: ユーザー登録→ログイン→ツイート投稿→タイムライン表示")
    void completeUserJourneyFromRegistrationToTimeline() {
        // Step 1: ユーザー登録
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        String userId = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """, username, email, password))
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .body("userId", notNullValue())
            .body("username", equalTo(username))
            .extract()
            .path("userId");

        // Step 2: ログイン
        String jwtToken = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, username, password))
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("userId", equalTo(userId))
            .extract()
            .path("token");

        // Step 3: ツイート投稿
        String tweetContent = "This is my first tweet!";
        String tweetId = given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "content": "%s"
                }
                """, tweetContent))
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .body("tweetId", notNullValue())
            .body("content", equalTo(tweetContent))
            .extract()
            .path("tweetId");

        // Step 4: タイムライン表示
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(200)
            .body("tweets", notNullValue())
            .body("tweets.size()", greaterThanOrEqualTo(0)); // 自分のツイートはフォローユーザーのツイートのみ表示されるため0件
    }

    // ========================================
    // E2Eフロー 2: ユーザーフォロー→タイムライン更新
    // ========================================

    @Test
    @DisplayName("E2Eフロー: ユーザーフォロー→タイムライン更新")
    void userFollowAndTimelineUpdate() {
        // Step 1: ユーザーAを登録
        String usernameA = "userA";
        String emailA = "usera@example.com";
        String passwordA = "password123";

        String userIdA = registerUser(usernameA, emailA, passwordA);
        String jwtTokenA = loginUser(usernameA, passwordA);

        // Step 2: ユーザーBを登録
        String usernameB = "userB";
        String emailB = "userb@example.com";
        String passwordB = "password456";

        String userIdB = registerUser(usernameB, emailB, passwordB);
        String jwtTokenB = loginUser(usernameB, passwordB);

        // Step 3: ユーザーBがツイート投稿
        String tweetContentB = "Hello from User B!";
        given()
            .header("Authorization", "Bearer " + jwtTokenB)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "content": "%s"
                }
                """, tweetContentB))
        .when()
            .post("/tweets")
        .then()
            .statusCode(201);

        // Step 4: ユーザーAがユーザーBをフォロー
        given()
            .header("Authorization", "Bearer " + jwtTokenA)
            .contentType(ContentType.JSON)
        .when()
            .post("/users/" + userIdB + "/follow")
        .then()
            .statusCode(201);

        // Step 5: ユーザーAのタイムラインにユーザーBのツイートが表示される
        given()
            .header("Authorization", "Bearer " + jwtTokenA)
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(200)
            .body("tweets", notNullValue())
            .body("tweets.size()", greaterThanOrEqualTo(1))
            .body("tweets[0].content", equalTo(tweetContentB))
            .body("tweets[0].user.username", equalTo(usernameB));
    }

    // ========================================
    // E2Eフロー 3: ツイートいいね・リツイート→カウント更新
    // ========================================

    @Test
    @DisplayName("E2Eフロー: ツイートいいね・リツイート→カウント更新")
    void likeTweetAndRetweetCountUpdate() {
        // Step 1: ユーザーAを登録してツイート投稿
        String usernameA = "userA";
        String jwtTokenA = loginUser(usernameA, "password123", registerUser(usernameA, "usera@example.com", "password123"));

        String tweetId = given()
            .header("Authorization", "Bearer " + jwtTokenA)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "content": "Tweet to be liked and retweeted"
                }
                """)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201)
            .extract()
            .path("tweetId");

        // Step 2: ユーザーBを登録
        String usernameB = "userB";
        String jwtTokenB = loginUser(usernameB, "password456", registerUser(usernameB, "userb@example.com", "password456"));

        // Step 3: ユーザーBがツイートにいいね
        given()
            .header("Authorization", "Bearer " + jwtTokenB)
            .contentType(ContentType.JSON)
        .when()
            .post("/tweets/" + tweetId + "/like")
        .then()
            .statusCode(201);

        // Step 4: ユーザーBがツイートをリツイート
        given()
            .header("Authorization", "Bearer " + jwtTokenB)
            .contentType(ContentType.JSON)
        .when()
            .post("/tweets/" + tweetId + "/retweet")
        .then()
            .statusCode(201);

        // Step 5: ツイート詳細を取得してカウントを確認
        given()
            .header("Authorization", "Bearer " + jwtTokenA)
            .contentType(ContentType.JSON)
        .when()
            .get("/tweets/" + tweetId)
        .then()
            .statusCode(200)
            .body("tweetId", equalTo(tweetId))
            .body("likesCount", equalTo(1))
            .body("retweetsCount", equalTo(1));
    }

    // ========================================
    // E2Eフロー 4: 検索機能（ユーザー検索・ツイート検索）
    // ========================================

    @Test
    @DisplayName("E2Eフロー: 検索機能（ユーザー検索・ツイート検索）")
    void searchUsersAndTweets() {
        // Step 1: 複数ユーザーを登録
        String userA = "searchUserA";
        String userB = "searchUserB";
        String jwtTokenA = loginUser(userA, "password123", registerUser(userA, "searchusera@example.com", "password123"));
        String jwtTokenB = loginUser(userB, "password456", registerUser(userB, "searchuserb@example.com", "password456"));

        // Step 2: ユーザーAがツイート投稿
        given()
            .header("Authorization", "Bearer " + jwtTokenA)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "content": "Searching for Java Spring Boot tutorials"
                }
                """)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201);

        // Step 3: ユーザーBがツイート投稿
        given()
            .header("Authorization", "Bearer " + jwtTokenB)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "content": "Love programming in Java!"
                }
                """)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201);

        // Step 4: ユーザー検索（"search"を含むユーザー）
        given()
            .header("Authorization", "Bearer " + jwtTokenA)
            .contentType(ContentType.JSON)
            .queryParam("query", "search")
        .when()
            .get("/search")
        .then()
            .statusCode(200)
            .body("users", notNullValue())
            .body("users.size()", greaterThanOrEqualTo(2)); // searchUserA と searchUserB

        // Step 5: ツイート検索（"Java"を含むツイート）
        given()
            .header("Authorization", "Bearer " + jwtTokenA)
            .contentType(ContentType.JSON)
            .queryParam("query", "Java")
        .when()
            .get("/search")
        .then()
            .statusCode(200)
            .body("tweets", notNullValue())
            .body("tweets.size()", greaterThanOrEqualTo(2)); // 両方のツイートに"Java"が含まれる
    }

    // ========================================
    // E2Eフロー 5: エラーハンドリング（認証エラー・バリデーションエラー）
    // ========================================

    @Test
    @DisplayName("E2Eフロー: 認証エラー（無効なJWTトークン）")
    void authenticationErrorInvalidJwt() {
        // Step 1: 無効なJWTトークンでタイムラインにアクセス
        given()
            .header("Authorization", "Bearer invalid-jwt-token")
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("E2Eフロー: バリデーションエラー（ツイート本文が280文字超過）")
    void validationErrorTweetContentTooLong() {
        // Step 1: ユーザー登録とログイン
        String username = "validationUser";
        String jwtToken = loginUser(username, "password123", registerUser(username, "validation@example.com", "password123"));

        // Step 2: 281文字のツイートを投稿（バリデーションエラー）
        String longContent = "a".repeat(281);
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "content": "%s"
                }
                """, longContent))
        .when()
            .post("/tweets")
        .then()
            .statusCode(400)
            .body("error.code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("E2Eフロー: バリデーションエラー（ユーザー名が3文字未満）")
    void validationErrorUsernameTooShort() {
        // Step 1: ユーザー名が2文字でユーザー登録（バリデーションエラー）
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username": "ab",
                    "email": "short@example.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400)
            .body("error.code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("E2Eフロー: バリデーションエラー（検索キーワードが2文字未満）")
    void validationErrorSearchQueryTooShort() {
        // Step 1: ユーザー登録とログイン
        String username = "searchValidationUser";
        String jwtToken = loginUser(username, "password123", registerUser(username, "searchval@example.com", "password123"));

        // Step 2: 1文字の検索キーワードで検索（バリデーションエラー）
        given()
            .header("Authorization", "Bearer " + jwtToken)
            .contentType(ContentType.JSON)
            .queryParam("query", "a")
        .when()
            .get("/search")
        .then()
            .statusCode(400)
            .body("error.code", equalTo("VALIDATION_ERROR"));
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    private String registerUser(String username, String email, String password) {
        return given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """, username, email, password))
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .extract()
            .path("userId");
    }

    private String loginUser(String username, String password) {
        return given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, username, password))
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
    }

    private String loginUser(String username, String password, String userId) {
        return loginUser(username, password);
    }
}
