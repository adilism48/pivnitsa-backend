package kg.megalab.pivnitsabackend.exception.booking;

public class InvalidBookingDataException extends RuntimeException {
    public InvalidBookingDataException(String message) {
        super(message);
    }
}
