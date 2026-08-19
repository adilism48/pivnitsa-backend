package kg.megalab.pivnitsabackend.exception.paymentexception;

public class InvalidPaymentStateException extends RuntimeException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
