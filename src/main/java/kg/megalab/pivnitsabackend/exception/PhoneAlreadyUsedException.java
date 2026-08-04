package kg.megalab.pivnitsabackend.exception;

public class PhoneAlreadyUsedException extends RuntimeException {
    public PhoneAlreadyUsedException(String message) {
        super(message);
    }
}
