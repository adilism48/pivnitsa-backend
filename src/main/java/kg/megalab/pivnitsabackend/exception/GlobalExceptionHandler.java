package kg.megalab.pivnitsabackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import kg.megalab.pivnitsabackend.exception.otpexceptions.InvalidOtpException;
import kg.megalab.pivnitsabackend.exception.otpexceptions.OtpAlreadySentException;
import kg.megalab.pivnitsabackend.exception.otpexceptions.OtpExpiredException;
import kg.megalab.pivnitsabackend.exception.paymentexception.InvalidPaymentStateException;
import kg.megalab.pivnitsabackend.exception.paymentexception.PaymentNotFoundException;
import kg.megalab.pivnitsabackend.exception.paymentexception.PaymentProviderException;
import kg.megalab.pivnitsabackend.exception.staffexception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentState(
            InvalidPaymentStateException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PaymentProviderException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProvider(
            PaymentProviderException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_GATEWAY.value(),
                HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                "Payment provider is temporarily unavailable",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(InvalidStaffCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStaffCredentials(InvalidStaffCredentialsException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(StaffEmailAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleStaffEmailAlreadyExist(StaffEmailAlreadyExistException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(LastOwnerException.class)
    public ResponseEntity<ErrorResponse> handleLastOwner(LastOwnerException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InsufficientStaffPermissionException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermission(InsufficientStaffPermissionException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(StaffNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStaffNotFound(StaffNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);


    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> BookingNotFound(BookingNotFoundException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidStaffRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStaffRole(InvalidStaffRoleException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(OtpAlreadySentException.class)
    public ResponseEntity<ErrorResponse> handleOtpAlreadySent(OtpAlreadySentException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getRetryAfterSeconds()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(errorResponse);
    }

    @ExceptionHandler({
            OtpExpiredException.class,
            InvalidOtpException.class,
            TooManyAttemptsException.class
    })
    public ResponseEntity<ErrorResponse> handleOtpValidationExceptions(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEventNotFound(
            EventNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Некорректный формат параметра: " + ex.getName(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(PhoneAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handlePhoneAlreadyUsed(
            PhoneAlreadyUsedException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(TokenBlacklistUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleTokenBlacklistUnavailable(TokenBlacklistUnavailableException ex, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Ошибка валидации");

        ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ActiveBookingExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveBookingExists(
            ActiveBookingExistsException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONTENT_TOO_LARGE.value(),
                HttpStatus.CONTENT_TOO_LARGE.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(errorResponse);
    }
}
