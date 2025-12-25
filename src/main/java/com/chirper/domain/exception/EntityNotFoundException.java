package com.chirper.domain.exception;

/**
 * EntityNotFoundException
 * エンティティが見つからない場合にスローされる例外
 *
 * 用途: User, Tweet, Follow などのエンティティが存在しない場合
 * HTTPステータス: 404 NOT_FOUND
 */
public class EntityNotFoundException extends DomainException {

    private static final String ERROR_CODE = "NOT_FOUND";

    /**
     * コンストラクタ
     *
     * @param message エラーメッセージ（例: "ユーザーが見つかりません"）
     */
    public EntityNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * コンストラクタ（原因となる例外を含む）
     *
     * @param message エラーメッセージ
     * @param cause 原因となる例外
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
