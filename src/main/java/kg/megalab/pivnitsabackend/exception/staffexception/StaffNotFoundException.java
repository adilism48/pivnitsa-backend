package kg.megalab.pivnitsabackend.exception.staffexception;

public class StaffNotFoundException extends RuntimeException {
    public StaffNotFoundException(String message) {
        super(message);
    }
}
