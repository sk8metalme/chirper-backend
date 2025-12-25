package com.chirper.presentation.exception;

/**
 * BusinessException
 * ビジネスロジック層の例外クラス
 *
 * @deprecated DomainException（およびその具体的なサブクラス）を使用してください。
 * このクラスはPresentation層に配置されており、Onion Architectureに準拠していません。
 * 新しいコードでは、Domain層のDomainExceptionを継承した例外クラスを使用してください。
 */
@Deprecated(since = "Phase 5", forRemoval = true)
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
