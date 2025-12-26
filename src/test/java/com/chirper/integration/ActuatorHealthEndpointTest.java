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
 * Spring Boot Actuator ヘルスエンドポイント統合テスト
 *
 * Task 21.1の要件:
 * - /actuator/healthエンドポイントでヘルスチェックを提供
 * - データベース接続、JVMメモリ使用率、ディスク使用率を監視
 * - メトリクス(CPU使用率、メモリ使用率、レスポンスタイム、エラー率)を収集
 *
 * Requirements: 10.1, 10.6
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("Actuator Health Endpoint 統合テスト")
class ActuatorHealthEndpointTest {

    @LocalServerPort
    private int port;

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
        // JWT設定（テスト用）
        registry.add("jwt.secret", () -> "test-secret-key-with-at-least-32-bytes-for-hs256-algorithm");
        registry.add("jwt.expiration-seconds", () -> "3600");
        // Actuator設定（詳細表示を有効化）
        registry.add("management.endpoint.health.show-details", () -> "always");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/actuator";
    }

    // ========================================
    // ヘルスエンドポイント基本テスト
    // ========================================

    @Test
    @DisplayName("/actuator/health - システムヘルスチェック - 200 OK")
    void actuatorHealth_shouldReturnUp() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/health")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("components", notNullValue());
    }

    @Test
    @DisplayName("/actuator/health - データベース接続状態を含む")
    void actuatorHealth_shouldIncludeDatabaseStatus() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/health")
        .then()
            .statusCode(200)
            .body("components.db", notNullValue())
            .body("components.db.status", equalTo("UP"))
            .body("components.db.details", notNullValue());
    }

    @Test
    @DisplayName("/actuator/health - ディスクスペース状態を含む")
    void actuatorHealth_shouldIncludeDiskSpaceStatus() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/health")
        .then()
            .statusCode(200)
            .body("components.diskSpace", notNullValue())
            .body("components.diskSpace.status", equalTo("UP"))
            .body("components.diskSpace.details.total", greaterThan(0L))
            .body("components.diskSpace.details.free", greaterThan(0L));
    }

    @Test
    @DisplayName("/actuator/health - Ping応答を含む")
    void actuatorHealth_shouldIncludePingStatus() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/health")
        .then()
            .statusCode(200)
            .body("components.ping", notNullValue())
            .body("components.ping.status", equalTo("UP"));
    }

    // ========================================
    // メトリクスエンドポイントテスト
    // ========================================

    @Test
    @DisplayName("/actuator/metrics - メトリクス一覧取得 - 200 OK")
    void actuatorMetrics_shouldReturnMetricsList() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/metrics")
        .then()
            .statusCode(200)
            .body("names", notNullValue())
            .body("names", hasItems("jvm.memory.used", "jvm.memory.max", "system.cpu.usage"));
    }

    @Test
    @DisplayName("/actuator/metrics/jvm.memory.used - JVMメモリ使用量取得")
    void actuatorMetrics_jvmMemoryUsed_shouldReturnMemoryDetails() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/metrics/jvm.memory.used")
        .then()
            .statusCode(200)
            .body("name", equalTo("jvm.memory.used"))
            .body("measurements", notNullValue())
            .body("measurements[0].value", greaterThan(0.0));
    }

    @Test
    @DisplayName("/actuator/metrics/system.cpu.usage - CPU使用率取得")
    void actuatorMetrics_systemCpuUsage_shouldReturnCpuDetails() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/metrics/system.cpu.usage")
        .then()
            .statusCode(200)
            .body("name", equalTo("system.cpu.usage"))
            .body("measurements", notNullValue());
    }

    @Test
    @DisplayName("/actuator/metrics/http.server.requests - HTTPリクエストメトリクス取得")
    void actuatorMetrics_httpServerRequests_shouldReturnRequestDetails() {
        // まずヘルスエンドポイントにアクセスしてメトリクスを生成
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/health")
        .then()
            .statusCode(200);

        // HTTPリクエストメトリクスを確認
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/metrics/http.server.requests")
        .then()
            .statusCode(200)
            .body("name", equalTo("http.server.requests"))
            .body("measurements", notNullValue());
    }

    // ========================================
    // インフォエンドポイントテスト
    // ========================================

    @Test
    @DisplayName("/actuator/info - アプリケーション情報取得 - 200 OK")
    void actuatorInfo_shouldReturnApplicationInfo() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/info")
        .then()
            .statusCode(200);
    }

    // ========================================
    // エンドポイント一覧テスト
    // ========================================

    @Test
    @DisplayName("/actuator - エンドポイント一覧取得 - 200 OK")
    void actuatorRoot_shouldReturnEndpointsList() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/")
        .then()
            .statusCode(200)
            .body("_links", notNullValue())
            .body("_links.health", notNullValue())
            .body("_links.info", notNullValue())
            .body("_links.metrics", notNullValue());
    }
}
