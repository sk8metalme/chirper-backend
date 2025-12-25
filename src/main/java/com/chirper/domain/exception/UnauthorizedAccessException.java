package com.chirper.domain.exception;

/**
 * UnauthorizedAccessException
 * 認証されていない、または認証に失敗した場合にスローされる例外
 *
 * 用途: ログインが必要な操作、認証トークンの検証失敗など
 * HTTPステータス: 401 UNAUTHORIZED
 */
public class UnauthorizedAccessException extends DomainException {

    private static final String ERROR_CODE = "UNAUTHORIZED";

    /**
     * コンストラクタ
     *
     * @param message エラーメッセージ（例: "認証が必要です"）
     */
    public UnauthorizedAccessException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * コンストラクタ（原因となる例外を含む）
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
