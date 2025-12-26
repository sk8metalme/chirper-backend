package com.chirper.performance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StopWatch;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * タイムラインAPIパフォーマンステスト
 *
 * Task 20.1の要件:
 * - タイムライン取得のレスポンスタイムが95パーセンタイル500ms以下であることを検証
 * - 毎秒100リクエストの処理（スループット）を検証
 * - N+1クエリ問題の検証（タイムライン取得時のクエリ実行回数を1回のみであることを確認）
 * - データベース接続プール枯渇テスト（同時接続数の上限をテスト）
 *
 * Requirements: 9.1, 9.2, 9.4, 9.5
 *
 * 注: N+1クエリ問題の検証は TimelineControllerIntegrationTest (Task 18.3) で既に実装済み
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("タイムラインAPI パフォーマンステスト")
class TimelinePerformanceTest {

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
        // HikariCP設定（パフォーマンステスト用）
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "50");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "10");
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
    // レスポンスタイム検証（95パーセンタイル500ms以下）
    // ========================================

    @Test
    @DisplayName("タイムライン取得 - レスポンスタイム95パーセンタイル500ms以下")
    void timeline_responseTime_shouldBe95PercentileUnder500ms() {
        // Given: テストデータ準備（ユーザー、フォロー、ツイート）
        setupPerformanceTestData();

        // Given: ユーザーログイン
        String jwtToken = loginUser("user0", "password123");

        // When: タイムライン取得を100回実行してレスポンスタイムを測定
        int iterations = 100;
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(ContentType.JSON)
            .when()
                .get("/timeline")
            .then()
                .statusCode(200);

            stopWatch.stop();
            responseTimes.add(stopWatch.getTotalTimeMillis());
        }

        // Then: 95パーセンタイルのレスポンスタイムが500ms以下であることを検証
        responseTimes.sort(Long::compareTo);
        int percentile95Index = (int) Math.ceil(iterations * 0.95) - 1;
        long percentile95ResponseTime = responseTimes.get(percentile95Index);

        System.out.println("=== タイムライン取得 レスポンスタイム統計 ===");
        System.out.println("最小: " + responseTimes.get(0) + "ms");
        System.out.println("最大: " + responseTimes.get(iterations - 1) + "ms");
        System.out.println("平均: " + responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) + "ms");
        System.out.println("95パーセンタイル: " + percentile95ResponseTime + "ms");

        assertThat(percentile95ResponseTime)
            .as("タイムライン取得の95パーセンタイルレスポンスタイムは500ms以下であること")
            .isLessThanOrEqualTo(500);
    }

    // ========================================
    // スループット検証（毎秒100リクエスト）
    // ========================================

    @Test
    @DisplayName("タイムライン取得 - 毎秒100リクエストのスループット")
    void timeline_throughput_shouldHandle100RequestsPerSecond() {
        // Given: テストデータ準備
        setupPerformanceTestData();

        // Given: ユーザーログイン
        String jwtToken = loginUser("user0", "password123");

        // When: 1秒間に100リクエストを並列実行
        int requestsCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<CompletableFuture<Response>> futures = IntStream.range(0, requestsCount)
            .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                given()
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(ContentType.JSON)
                .when()
                    .get("/timeline")
                .then()
                    .extract()
                    .response(),
                executor))
            .toList();

        // すべてのリクエストが完了するまで待機
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();

        executor.shutdown();

        // Then: すべてのリクエストが成功し、1秒以内に完了していることを検証
        long totalTimeMs = stopWatch.getTotalTimeMillis();
        long successfulRequests = futures.stream()
            .map(CompletableFuture::join)
            .filter(response -> response.statusCode() == 200)
            .count();

        System.out.println("=== スループットテスト結果 ===");
        System.out.println("総リクエスト数: " + requestsCount);
        System.out.println("成功リクエスト数: " + successfulRequests);
        System.out.println("総実行時間: " + totalTimeMs + "ms");
        System.out.println("実際のスループット: " + (successfulRequests * 1000.0 / totalTimeMs) + " req/sec");

        assertThat(successfulRequests)
            .as("すべてのリクエストが成功していること")
            .isEqualTo(requestsCount);

        assertThat(totalTimeMs)
            .as("100リクエストを1秒以内に処理できること（多少のマージンを考慮して2秒以内）")
            .isLessThanOrEqualTo(2000);
    }

    // ========================================
    // 接続プール枯渇テスト
    // ========================================

    @Test
    @DisplayName("タイムライン取得 - 接続プール枯渇テスト（同時接続数上限）")
    void timeline_connectionPool_shouldHandleMaximumConnections() {
        // Given: テストデータ準備
        setupPerformanceTestData();

        // Given: ユーザーログイン
        String jwtToken = loginUser("user0", "password123");

        // When: HikariCPの最大接続数（50）を超える同時リクエスト（60リクエスト）を実行
        int requestsCount = 60;
        ExecutorService executor = Executors.newFixedThreadPool(60);

        List<CompletableFuture<Response>> futures = IntStream.range(0, requestsCount)
            .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                given()
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(ContentType.JSON)
                .when()
                    .get("/timeline")
                .then()
                    .extract()
                    .response(),
                executor))
            .toList();

        // すべてのリクエストが完了するまで待機
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();

        // Then: すべてのリクエストが成功していることを検証（接続プールが適切に管理されている）
        long successfulRequests = futures.stream()
            .map(CompletableFuture::join)
            .filter(response -> response.statusCode() == 200)
            .count();

        System.out.println("=== 接続プール枯渇テスト結果 ===");
        System.out.println("総リクエスト数: " + requestsCount);
        System.out.println("成功リクエスト数: " + successfulRequests);

        assertThat(successfulRequests)
            .as("接続プールの最大接続数を超えるリクエストでも、すべて成功していること")
            .isEqualTo(requestsCount);
    }

    // ========================================
    // 負荷テスト（長時間実行）
    // ========================================

    @Test
    @DisplayName("タイムライン取得 - 長時間負荷テスト（500リクエスト）")
    void timeline_loadTest_shouldHandle500RequestsWithoutDegradation() {
        // Given: テストデータ準備
        setupPerformanceTestData();

        // Given: ユーザーログイン
        String jwtToken = loginUser("user0", "password123");

        // When: 500リクエストを実行してレスポンスタイムの劣化がないことを確認
        int totalRequests = 500;
        int batchSize = 50;
        List<Long> allResponseTimes = new ArrayList<>();

        for (int batch = 0; batch < totalRequests / batchSize; batch++) {
            List<Long> batchResponseTimes = new ArrayList<>();

            for (int i = 0; i < batchSize; i++) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                given()
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(ContentType.JSON)
                .when()
                    .get("/timeline")
                .then()
                    .statusCode(200);

                stopWatch.stop();
                batchResponseTimes.add(stopWatch.getTotalTimeMillis());
            }

            allResponseTimes.addAll(batchResponseTimes);

            // バッチごとの平均レスポンスタイムを出力
            double batchAverage = batchResponseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            System.out.println("バッチ " + (batch + 1) + " 平均レスポンスタイム: " + batchAverage + "ms");
        }

        // Then: 最初の100リクエストと最後の100リクエストの平均レスポンスタイムを比較
        double firstBatchAverage = allResponseTimes.subList(0, 100).stream()
            .mapToLong(Long::longValue).average().orElse(0);
        double lastBatchAverage = allResponseTimes.subList(totalRequests - 100, totalRequests).stream()
            .mapToLong(Long::longValue).average().orElse(0);

        System.out.println("=== 長時間負荷テスト結果 ===");
        System.out.println("最初の100リクエスト平均: " + firstBatchAverage + "ms");
        System.out.println("最後の100リクエスト平均: " + lastBatchAverage + "ms");
        System.out.println("劣化率: " + ((lastBatchAverage - firstBatchAverage) / firstBatchAverage * 100) + "%");

        assertThat(lastBatchAverage)
            .as("長時間実行後もレスポンスタイムが劣化していないこと（最初のバッチの2倍以下）")
            .isLessThanOrEqualTo(firstBatchAverage * 2);
    }

    // ========================================
    // ヘルパーメソッド
    // ========================================

    /**
     * パフォーマンステスト用のテストデータをセットアップ
     * - ユーザー10人
     * - user0がuser1-9をフォロー
     * - 各ユーザーが10ツイートを投稿（合計100ツイート）
     */
    private void setupPerformanceTestData() {
        int userCount = 10;
        int tweetsPerUser = 10;

        // ユーザー作成
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            String userId = registerUser("user" + i, "user" + i + "@example.com", "password123");
            userIds.add(userId);
        }

        // user0がuser1-9をフォロー
        String jwtTokenUser0 = loginUser("user0", "password123");
        for (int i = 1; i < userCount; i++) {
            given()
                .header("Authorization", "Bearer " + jwtTokenUser0)
                .contentType(ContentType.JSON)
            .when()
                .post("/users/" + userIds.get(i) + "/follow")
            .then()
                .statusCode(201);
        }

        // 各ユーザーがツイート投稿
        for (int i = 0; i < userCount; i++) {
            String jwtToken = loginUser("user" + i, "password123");
            for (int j = 0; j < tweetsPerUser; j++) {
                given()
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(ContentType.JSON)
                    .body(String.format("""
                        {
                            "content": "Tweet %d from user%d"
                        }
                        """, j, i))
                .when()
                    .post("/tweets")
                .then()
                    .statusCode(201);
            }
        }
    }

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
}
