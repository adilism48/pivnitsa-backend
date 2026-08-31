package kg.megalab.pivnitsabackend.exception.booking;

public class GuestsExceedCapacityException extends RuntimeException {
    public GuestsExceedCapacityException(String message) {
        super(message);
    }
}
