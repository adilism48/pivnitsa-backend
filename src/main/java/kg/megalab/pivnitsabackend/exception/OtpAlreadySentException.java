package kg.megalab.pivnitsabackend.exception;

public class OtpAlreadySentException extends RuntimeException {

    private final long retryAfterSeconds;

    public OtpAlreadySentException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}