package kg.megalab.pivnitsabackend.exception.tables;

public class TableNumberAlreadyExistException extends RuntimeException {
    public TableNumberAlreadyExistException(String message) {
        super(message);
    }
}
