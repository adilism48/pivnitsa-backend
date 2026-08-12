package kg.megalab.pivnitsabackend.exception;

public class InvalidStaffCredentialsException extends RuntimeException {
    public InvalidStaffCredentialsException (String message) {
        super(message);
    }
}
