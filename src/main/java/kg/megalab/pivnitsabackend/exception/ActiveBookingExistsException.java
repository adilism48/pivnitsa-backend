package kg.megalab.pivnitsabackend.exception;

public class ActiveBookingExistsException extends RuntimeException {
    public ActiveBookingExistsException(String message) {
        super(message);
    }
}
