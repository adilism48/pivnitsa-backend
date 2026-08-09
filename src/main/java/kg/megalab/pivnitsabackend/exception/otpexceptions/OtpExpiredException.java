package kg.megalab.pivnitsabackend.exception.otpexceptions;

public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
