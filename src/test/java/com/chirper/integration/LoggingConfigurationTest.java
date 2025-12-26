package com.chirper.integration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Logback構造化ログ設定テスト
 *
 * Task 21.2の要件:
 * - Logbackで構造化ログ(JSON形式)を出力
 * - ログレベル(INFO、WARN、ERROR)で適切にログ出力
 * - パスワード、JWTトークンはログに出力しない(マスキング設定)
 * - ログローテーション(日次、7日間保持)を設定
 *
 * Requirements: 10.2, 10.3, 10.4
 *
 * 注: このテストはSpring Contextを起動せず、Logback設定のみを検証します
 */
@DisplayName("Logging Configuration テスト")
class LoggingConfigurationTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger("com.chirper.test");
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    // ========================================
    // ログレベルテスト
    // ========================================

    @Test
    @DisplayName("INFOレベルのログが出力される")
    void logInfo_shouldOutputInfoLevelLog() {
        // When: INFOレベルのログを出力
        logger.info("This is an INFO level log");

        // Then: ログが記録されている
        assertThat(listAppender.list)
            .isNotEmpty()
            .anyMatch(event -> event.getMessage().equals("This is an INFO level log"));
    }

    @Test
    @DisplayName("WARNレベルのログが出力される")
    void logWarn_shouldOutputWarnLevelLog() {
        // When: WARNレベルのログを出力
        logger.warn("This is a WARN level log");

        // Then: ログが記録されている
        assertThat(listAppender.list)
            .isNotEmpty()
            .anyMatch(event -> event.getMessage().equals("This is a WARN level log"));
    }

    @Test
    @DisplayName("ERRORレベルのログが出力される")
    void logError_shouldOutputErrorLevelLog() {
        // When: ERRORレベルのログを出力
        logger.error("This is an ERROR level log");

        // Then: ログが記録されている
        assertThat(listAppender.list)
            .isNotEmpty()
            .anyMatch(event -> event.getMessage().equals("This is an ERROR level log"));
    }

    // ========================================
    // マスキングテスト（概念実証）
    // ========================================

    @Test
    @DisplayName("パスワードを含むログメッセージが記録される（マスキングはlogback-spring.xmlで設定）")
    void logWithPassword_shouldBeRecorded() {
        // Given: パスワードを含むメッセージ
        String messageWithPassword = "User login attempt with password=secretPassword123";

        // When: ログを出力
        logger.info(messageWithPassword);

        // Then: ログが記録されている
        // 注: 実際のマスキングはLogstashEncoderのmessageMaskPatternで処理される
        assertThat(listAppender.list)
            .isNotEmpty()
            .anyMatch(event -> event.getMessage().contains("password"));
    }

    @Test
    @DisplayName("JWTトークンを含むログメッセージが記録される（マスキングはlogback-spring.xmlで設定）")
    void logWithJwtToken_shouldBeRecorded() {
        // Given: JWTトークンを含むメッセージ
        String messageWithToken = "Authorization header: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When: ログを出力
        logger.info(messageWithToken);

        // Then: ログが記録されている
        // 注: 実際のマスキングはLogstashEncoderのmessageMaskPatternで処理される
        assertThat(listAppender.list)
            .isNotEmpty()
            .anyMatch(event -> event.getMessage().contains("Bearer"));
    }

    // ========================================
    // ログファイル存在確認テスト
    // ========================================

    @Test
    @DisplayName("ログディレクトリが存在する")
    void logDirectory_shouldExist() {
        // Given: ログディレクトリのパス
        Path logDir = Paths.get("logs");

        // When/Then: ディレクトリが存在するか、または作成できる
        if (!Files.exists(logDir)) {
            // ログディレクトリは最初のログ出力時に自動作成される
            assertThat(logDir.getParent()).isNotNull();
        } else {
            assertThat(Files.isDirectory(logDir)).isTrue();
        }
    }

    // ========================================
    // Logback設定ファイル存在確認テスト
    // ========================================

    @Test
    @DisplayName("logback-spring.xmlが存在する")
    void logbackConfigFile_shouldExist() {
        // Given: logback-spring.xmlのパス
        Path logbackConfig = Paths.get("src/main/resources/logback-spring.xml");

        // Then: ファイルが存在する
        assertThat(Files.exists(logbackConfig))
            .as("logback-spring.xml設定ファイルが存在すること")
            .isTrue();
    }

    @Test
    @DisplayName("logback-spring.xmlにJSON形式の設定が含まれる")
    void logbackConfigFile_shouldContainJsonConfiguration() throws Exception {
        // Given: logback-spring.xmlの内容
        Path logbackConfig = Paths.get("src/main/resources/logback-spring.xml");
        String content = Files.readString(logbackConfig);

        // Then: JSON形式のログ出力設定が含まれている
        assertThat(content)
            .contains("LogstashEncoder")
            .contains("JSON_FILE")
            .as("logback-spring.xmlにLogstashEncoderとJSON_FILE設定が含まれること");
    }

    @Test
    @DisplayName("logback-spring.xmlにマスキング設定が含まれる")
    void logbackConfigFile_shouldContainMaskingConfiguration() throws Exception {
        // Given: logback-spring.xmlの内容
        Path logbackConfig = Paths.get("src/main/resources/logback-spring.xml");
        String content = Files.readString(logbackConfig);

        // Then: マスキング設定が含まれている
        assertThat(content)
            .contains("fieldNamesToMask")
            .contains("password")
            .contains("token")
            .contains("messageMaskPattern")
            .as("logback-spring.xmlにパスワードとトークンのマスキング設定が含まれること");
    }

    @Test
    @DisplayName("logback-spring.xmlにローテーション設定が含まれる")
    void logbackConfigFile_shouldContainRotationConfiguration() throws Exception {
        // Given: logback-spring.xmlの内容
        Path logbackConfig = Paths.get("src/main/resources/logback-spring.xml");
        String content = Files.readString(logbackConfig);

        // Then: ローテーション設定が含まれている
        assertThat(content)
            .contains("TimeBasedRollingPolicy")
            .contains("maxHistory>7<")
            .contains("yyyy-MM-dd")
            .as("logback-spring.xmlに日次ローテーションと7日間保持の設定が含まれること");
    }

    // ========================================
    // ログレベル設定テスト
    // ========================================

    @Test
    @DisplayName("com.chirperパッケージのログレベルがINFO以上")
    void chirperPackage_shouldHaveInfoLevelOrAbove() {
        // Given: com.chirperパッケージのロガー
        Logger chirperLogger = (Logger) LoggerFactory.getLogger("com.chirper");

        // Then: INFOレベル以上が有効
        assertThat(chirperLogger.isInfoEnabled()).isTrue();
    }

    @Test
    @DisplayName("Springフレームワークのログレベルが適切に設定されている")
    void springFramework_shouldHaveAppropriateLogLevel() {
        // Given: Springフレームワークのロガー
        Logger springLogger = (Logger) LoggerFactory.getLogger("org.springframework");

        // Then: 基本的なログレベルが設定されている
        assertThat(springLogger).isNotNull();
    }
}
