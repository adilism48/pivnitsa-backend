package kg.megalab.pivnitsabackend.exception;

public class OtpAlreadySentException extends RuntimeException {
    public OtpAlreadySentException(String message) {
        super(message);
    }
}
