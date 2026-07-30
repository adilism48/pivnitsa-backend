package kg.megalab.pivnitsabackend.otp;

public class OtpSendException extends RuntimeException {
    public OtpSendException(String message, Throwable cause) {
        super(message, cause);
    }
}