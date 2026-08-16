package kg.megalab.pivnitsabackend.exception.staffexception;

public class InvalidStaffCredentialsException extends RuntimeException {
    public InvalidStaffCredentialsException(String message) {
        super(message);
    }
}
