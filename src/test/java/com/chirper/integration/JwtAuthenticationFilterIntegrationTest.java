package com.chirper.integration;

import com.chirper.infrastructure.security.JwtUtil;
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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * JwtAuthenticationFilter統合テスト（TestContainers + RestAssured）
 *
 * JWT認証フローのエンドツーエンドテスト
 * - 有効なJWTトークンでのアクセス
 * - 無効なJWTトークンでの401エラー
 * - 期限切れJWTトークンでの401エラー
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("JwtAuthenticationFilter 統合テスト")
class JwtAuthenticationFilterIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("chirper_test")
            .withUsername("test_user");
            // TestContainersはデフォルトでランダムパスワードを自動生成

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

        // テスト間のデータクリーンアップ（テスト独立性確保）
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    }

    // ========================================
    // JWT認証成功テスト
    // ========================================

    @Test
    @DisplayName("有効なJWTトークンでタイムラインにアクセス - 認証成功")
    void accessTimeline_withValidJwt_shouldSucceed() {
        // Given: ユーザー登録とログインでJWTを取得
        String username = "testuser";
        String password = "password123";

        registerUser(username, "test@example.com", password);
        String jwt = loginUser(username, password);

        // When: JWTトークンを使用してタイムラインにアクセス
        // Then: 200 OKが返される
        given()
            .header("Authorization", "Bearer " + jwt)
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(200);
    }

    // ========================================
    // JWT認証失敗テスト - 401 Unauthorized
    // ========================================

    @Test
    @DisplayName("Authorizationヘッダーなし - 401 Unauthorized")
    void accessTimeline_withoutAuthHeader_shouldReturn401() {
        // When: Authorizationヘッダーなしでタイムラインにアクセス
        // Then: 401 Unauthorizedが返される
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Bearerプレフィックスなし - 401 Unauthorized")
    void accessTimeline_withoutBearerPrefix_shouldReturn401() {
        // Given: 有効なJWTトークンを取得（Bearerプレフィックスなしで使用）
        String username = "testuser";
        String password = "password123";

        registerUser(username, "test@example.com", password);
        String jwt = loginUser(username, password);

        // When: Bearerプレフィックスなしでタイムラインにアクセス
        // Then: 401 Unauthorizedが返される
        given()
            .header("Authorization", jwt)  // "Bearer " なし
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("無効な署名のJWTトークン - 401 Unauthorized")
    void accessTimeline_withInvalidSignature_shouldReturn401() {
        // Given: 有効なJWTトークンを取得し、署名を改ざん
        String username = "testuser";
        String password = "password123";

        registerUser(username, "test@example.com", password);
        String jwt = loginUser(username, password);

        // JWT署名を改ざん（最後の文字を変更）
        String tamperedJwt = jwt.substring(0, jwt.length() - 1) + "X";

        // When: 改ざんされたJWTでタイムラインにアクセス
        // Then: 401 Unauthorizedが返される
        given()
            .header("Authorization", "Bearer " + tamperedJwt)
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("不正な形式のJWTトークン - 401 Unauthorized")
    void accessTimeline_withMalformedJwt_shouldReturn401() {
        // Given: 不正な形式のJWTトークン
        String malformedJwt = "not.a.valid.jwt.token";

        // When: 不正な形式のJWTでタイムラインにアクセス
        // Then: 401 Unauthorizedが返される
        given()
            .header("Authorization", "Bearer " + malformedJwt)
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("有効期限切れのJWTトークン - 401 Unauthorized")
    void accessTimeline_withExpiredJwt_shouldReturn401() {
        // Given: 有効期限が-1秒（即座に期限切れ）のJWTトークンを生成
        String userId = UUID.randomUUID().toString();
        String expiredJwt = jwtUtil.generateTokenWithCustomExpiration(userId, -1);

        // When: 期限切れJWTでタイムラインにアクセス
        // Then: 401 Unauthorizedが返される
        given()
            .header("Authorization", "Bearer " + expiredJwt)
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("異なるシークレットキーで生成されたJWT - 401 Unauthorized")
    void accessTimeline_withDifferentSecret_shouldReturn401() {
        // Given: 異なるシークレットキーで生成されたJWT
        // 注: これは外部から不正に生成されたJWTを模擬
        String fakeJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When: 異なるシークレットで生成されたJWTでタイムラインにアクセス
        // Then: 401 Unauthorizedが返される
        given()
            .header("Authorization", "Bearer " + fakeJwt)
            .contentType(ContentType.JSON)
        .when()
            .get("/timeline")
        .then()
            .statusCode(401);
    }

    // ========================================
    // 保護されたエンドポイントへのアクセステスト
    // ========================================

    @Test
    @DisplayName("ツイート投稿 - 有効なJWTで成功")
    void createTweet_withValidJwt_shouldSucceed() {
        // Given: ユーザー登録とログイン
        String username = "testuser";
        String password = "password123";

        registerUser(username, "test@example.com", password);
        String jwt = loginUser(username, password);

        // When: 有効なJWTでツイート投稿
        // Then: 201 Createdが返される
        given()
            .header("Authorization", "Bearer " + jwt)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "content": "This is a test tweet"
                }
                """)
        .when()
            .post("/tweets")
        .then()
            .statusCode(201);
    }

    @Test
    @DisplayName("ツイート投稿 - JWTなしで401 Unauthorized")
    void createTweet_withoutJwt_shouldReturn401() {
        // When: JWTなしでツイート投稿
        // Then: 401 Unauthorizedが返される
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "content": "This is a test tweet"
                }
                """)
        .when()
            .post("/tweets")
        .then()
            .statusCode(401);
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    private void registerUser(String username, String email, String password) {
        given()
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
            .statusCode(201);
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
}
