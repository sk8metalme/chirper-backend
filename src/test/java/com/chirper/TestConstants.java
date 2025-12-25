package com.chirper;

/**
 * テスト用共通定数クラス
 * テスト全体で使用する定数を集約管理する
 */
public final class TestConstants {

    /**
     * インスタンス化を防ぐプライベートコンストラクタ
     */
    private TestConstants() {
        throw new AssertionError("Cannot instantiate TestConstants");
    }

    /**
     * JWT認証関連の定数
     */
    public static final class Authentication {
        private Authentication() {
            throw new AssertionError("Cannot instantiate Authentication");
        }

        /**
         * テスト用JWTシークレットキー（32バイト以上）
         */
        public static final String JWT_SECRET = "test-secret-key-with-at-least-32-bytes-for-hs256-algorithm";

        /**
         * テスト用の異なるJWTシークレットキー（32バイト以上）
         * 異なる署名検証テストに使用
         */
        public static final String DIFFERENT_JWT_SECRET = "different-secret-key-with-at-least-32-bytes-for-hs256";
    }

    /**
     * テストデータ関連の定数
     */
    public static final class TestData {
        private TestData() {
            throw new AssertionError("Cannot instantiate TestData");
        }

        /**
         * テスト用ユーザー名
         */
        public static final String TEST_USERNAME = "testuser";

        /**
         * テスト用メールアドレス
         */
        public static final String TEST_EMAIL = "test@example.com";

        /**
         * テスト用パスワード
         */
        public static final String TEST_PASSWORD = "password123";

        /**
         * テスト用ツイート内容
         */
        public static final String TEST_TWEET_CONTENT = "Test tweet";
    }
}
