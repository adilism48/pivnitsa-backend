package kg.megalab.pivnitsabackend.exception.tables;

public class TableNotFoundException extends RuntimeException {
    public TableNotFoundException(String message) {
        super(message);
    }
}
