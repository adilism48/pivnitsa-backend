package kg.megalab.pivnitsabackend.exception.paymentexception;

import lombok.Getter;

@Getter
public class PaymentProviderException extends RuntimeException {

    private final String providerCode;
    private final String providerErrorCode;

    public PaymentProviderException(
            String message,
            String providerCode,
            String providerErrorCode
    ) {
        super(message);
        this.providerCode = providerCode;
        this.providerErrorCode = providerErrorCode;
    }

    public PaymentProviderException(
            String message,
            String providerCode,
            String providerErrorCode,
            Throwable cause
    ) {
        super(message, cause);
        this.providerCode = providerCode;
        this.providerErrorCode = providerErrorCode;
    }
}
