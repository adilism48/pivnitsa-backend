package kg.megalab.pivnitsabackend.exception.otpexceptions;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}
