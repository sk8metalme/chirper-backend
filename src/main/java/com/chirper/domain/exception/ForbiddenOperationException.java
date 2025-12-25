package com.chirper.domain.exception;

/**
 * ForbiddenOperationException
 * 認証はされているが、権限がない操作を実行しようとした場合にスローされる例外
 *
 * 用途: 他人のツイート削除、他人のプロフィール編集など
 * HTTPステータス: 403 FORBIDDEN
 */
public class ForbiddenOperationException extends DomainException {

    private static final String ERROR_CODE = "FORBIDDEN";

    /**
     * コンストラクタ
     *
     * @param message エラーメッセージ（例: "このツイートを削除する権限がありません"）
     */
    public ForbiddenOperationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * コンストラクタ（原因となる例外を含む）
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public ForbiddenOperationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
