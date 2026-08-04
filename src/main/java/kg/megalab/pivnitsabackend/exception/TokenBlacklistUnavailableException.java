package kg.megalab.pivnitsabackend.exception;

public class TokenBlacklistUnavailableException extends RuntimeException {
    public TokenBlacklistUnavailableException(String message) {
        super(message);
    }
}
