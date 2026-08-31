package kg.megalab.pivnitsabackend.exception.booking;

public class TableNotAvailableException extends RuntimeException {
    public TableNotAvailableException(String message) {
        super(message);
    }
}
