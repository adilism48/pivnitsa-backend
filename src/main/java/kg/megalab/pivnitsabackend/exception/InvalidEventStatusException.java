package kg.megalab.pivnitsabackend.exception;

public class InvalidEventStatusException extends RuntimeException {
    public InvalidEventStatusException(String message) {
        super(message);
    }
}
