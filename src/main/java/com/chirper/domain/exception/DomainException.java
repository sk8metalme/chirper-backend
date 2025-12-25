package com.chirper.domain.exception;

/**
 * DomainException
 * ドメイン層の基底例外クラス
 *
 * すべてのドメイン層の例外はこのクラスを継承する
 * Onion Architectureに準拠し、Domain層に配置される
 */
public class DomainException extends RuntimeException {

    private final String errorCode;

    /**
     * コンストラクタ
     *
     * @param errorCode エラーコード（例: "NOT_FOUND", "CONFLICT", "BAD_REQUEST"）
     * @param message エラーメッセージ
     */
    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * コンストラクタ（原因となる例外を含む）
     *
     * @param errorCode エラーコード
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * エラーコードを取得
     *
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }
}
