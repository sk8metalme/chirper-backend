package com.chirper.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * AuthController統合テスト（TestContainers + RestAssured）
 *
 * 実際のPostgreSQLデータベースを使用したエンドツーエンドテスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("AuthController 統合テスト")
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

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
    }

    // ========================================
    // ユーザー登録テスト
    // ========================================

    @Test
    @DisplayName("POST /auth/register - 有効なリクエストでユーザー登録が成功する（201 Created）")
    void registerUser_withValidRequest_shouldReturn201() {
        String requestBody = """
            {
                "username": "testuser",
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .body("userId", notNullValue())
            .body("message", equalTo("ユーザー登録が完了しました"));
    }

    @Test
    @DisplayName("POST /auth/register - バリデーションエラーの場合400を返す（ユーザー名が短すぎる）")
    void registerUser_withInvalidUsername_shouldReturn400() {
        String requestBody = """
            {
                "username": "ab",
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /auth/register - 無効なメールアドレスの場合400を返す")
    void registerUser_withInvalidEmail_shouldReturn400() {
        String requestBody = """
            {
                "username": "testuser",
                "email": "invalid-email",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /auth/register - ユーザー名が重複している場合409を返す")
    void registerUser_withDuplicateUsername_shouldReturn409() {
        // 最初のユーザー登録
        String requestBody1 = """
            {
                "username": "duplicateuser",
                "email": "user1@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody1)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201);

        // 同じユーザー名で再登録
        String requestBody2 = """
            {
                "username": "duplicateuser",
                "email": "user2@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody2)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(409)
            .body("code", equalTo("CONFLICT"))
            .body("message", containsString("ユーザー名は既に使用されています"));
    }

    @Test
    @DisplayName("POST /auth/register - メールアドレスが重複している場合409を返す")
    void registerUser_withDuplicateEmail_shouldReturn409() {
        // 最初のユーザー登録
        String requestBody1 = """
            {
                "username": "user1",
                "email": "duplicate@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody1)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201);

        // 同じメールアドレスで再登録
        String requestBody2 = """
            {
                "username": "user2",
                "email": "duplicate@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody2)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(409)
            .body("code", equalTo("CONFLICT"));
    }

    // ========================================
    // ユーザーログインテスト
    // ========================================

    @Test
    @DisplayName("POST /auth/login - 有効な認証情報でログインが成功する（200 OK）")
    void loginUser_withValidCredentials_shouldReturn200() {
        // ユーザー登録
        String registerBody = """
            {
                "username": "loginuser",
                "email": "login@example.com",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(registerBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201);

        // ログイン
        String loginBody = """
            {
                "username": "loginuser",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("userId", notNullValue())
            .body("username", equalTo("loginuser"))
            .body("expiresAt", notNullValue());
    }

    @Test
    @DisplayName("POST /auth/login - 誤ったパスワードの場合401を返す")
    void loginUser_withWrongPassword_shouldReturn401() {
        // ユーザー登録
        String registerBody = """
            {
                "username": "wrongpassuser",
                "email": "wrongpass@example.com",
                "password": "correctpassword"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(registerBody)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201);

        // 誤ったパスワードでログイン
        String loginBody = """
            {
                "username": "wrongpassuser",
                "password": "wrongpassword"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("code", equalTo("UNAUTHORIZED"))
            .body("message", containsString("認証に失敗しました"));
    }

    @Test
    @DisplayName("POST /auth/login - 存在しないユーザー名の場合401を返す")
    void loginUser_withNonExistentUsername_shouldReturn401() {
        String loginBody = """
            {
                "username": "nonexistentuser",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("code", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("POST /auth/login - バリデーションエラーの場合400を返す（ユーザー名が空）")
    void loginUser_withEmptyUsername_shouldReturn400() {
        String loginBody = """
            {
                "username": "",
                "password": "password123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(loginBody)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }
}
