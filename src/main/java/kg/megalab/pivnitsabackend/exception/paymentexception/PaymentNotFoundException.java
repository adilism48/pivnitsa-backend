package kg.megalab.pivnitsabackend.exception.paymentexception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String message) {
        super(message);
    }
}
