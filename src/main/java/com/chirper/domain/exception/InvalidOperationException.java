package com.chirper.domain.exception;

/**
 * InvalidOperationException
 * 無効な操作が実行された場合にスローされる例外
 *
 * 用途: 自己フォロー禁止、無効なパラメータ、ビジネスルール違反など
 * HTTPステータス: 400 BAD_REQUEST
 */
public class InvalidOperationException extends DomainException {

    private static final String ERROR_CODE = "BAD_REQUEST";

    /**
     * コンストラクタ
     *
     * @param message エラーメッセージ（例: "自分自身をフォローすることはできません"）
     */
    public InvalidOperationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * コンストラクタ（原因となる例外を含む）
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public InvalidOperationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
