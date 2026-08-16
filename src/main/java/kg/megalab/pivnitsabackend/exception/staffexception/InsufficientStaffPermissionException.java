package kg.megalab.pivnitsabackend.exception.staffexception;

public class InsufficientStaffPermissionException extends RuntimeException {
    public InsufficientStaffPermissionException(String message) {
        super(message);
    }
}
