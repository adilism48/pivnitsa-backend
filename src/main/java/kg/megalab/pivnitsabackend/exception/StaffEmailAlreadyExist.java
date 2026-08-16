package kg.megalab.pivnitsabackend.exception;

public class StaffEmailAlreadyExist extends RuntimeException {
    public StaffEmailAlreadyExist(String message) {
        super(message);
    }
}
