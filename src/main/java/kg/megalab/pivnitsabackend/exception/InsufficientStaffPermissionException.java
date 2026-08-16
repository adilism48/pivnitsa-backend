package kg.megalab.pivnitsabackend.exception;

public class InsufficientStaffPermissionException extends RuntimeException {
    public InsufficientStaffPermissionException(String message) {
        super(message);
    }
}
