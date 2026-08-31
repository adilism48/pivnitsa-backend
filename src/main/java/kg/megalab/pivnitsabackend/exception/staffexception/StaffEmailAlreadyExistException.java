package kg.megalab.pivnitsabackend.exception.staffexception;

public class StaffEmailAlreadyExistException extends RuntimeException {
    public StaffEmailAlreadyExistException(String message) {
        super(message);
    }
}
