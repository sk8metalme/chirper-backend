package com.chirper.domain.exception;

/**
 * DuplicateEntityException
 * エンティティが重複している場合にスローされる例外
 *
 * 用途: Username, Email などの一意制約違反の場合
 * HTTPステータス: 409 CONFLICT
 */
public class DuplicateEntityException extends DomainException {

    private static final String ERROR_CODE = "CONFLICT";

    /**
     * コンストラクタ
     *
     * @param message エラーメッセージ（例: "このユーザー名は既に使用されています"）
     */
    public DuplicateEntityException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * コンストラクタ（原因となる例外を含む）
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public DuplicateEntityException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
