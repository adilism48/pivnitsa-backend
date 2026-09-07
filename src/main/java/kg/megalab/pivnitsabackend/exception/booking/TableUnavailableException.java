package kg.megalab.pivnitsabackend.exception.booking;

public class TableUnavailableException extends RuntimeException {
    public TableUnavailableException(String message) {
        super(message);
    }
}