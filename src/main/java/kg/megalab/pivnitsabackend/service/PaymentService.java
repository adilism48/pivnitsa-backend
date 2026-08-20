package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.*;
import kg.megalab.pivnitsabackend.exception.BookingNotFoundException;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.exception.paymentexception.InvalidPaymentStateException;
import kg.megalab.pivnitsabackend.exception.paymentexception.PaymentNotFoundException;
import kg.megalab.pivnitsabackend.exception.paymentexception.PaymentProviderException;
import kg.megalab.pivnitsabackend.payment.PaymentProvider;
import kg.megalab.pivnitsabackend.payment.PaymentProviderRegistry;
import kg.megalab.pivnitsabackend.payment.dto.payments.CreatePaymentCommand;
import kg.megalab.pivnitsabackend.payment.dto.payments.CreatePaymentResult;
import kg.megalab.pivnitsabackend.payment.dto.payments.PaymentInitiationResult;
import kg.megalab.pivnitsabackend.repository.BookingRepository;
import kg.megalab.pivnitsabackend.repository.PaymentRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String CURRENCY_KGS = "KGS";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentProviderRegistry providerRegistry;

    public PaymentInitiationResult createPayment(
            Long bookingId,
            String authenticatedPhone,
            String providerCode,
            String merchantOrderId,
            URI successUrl,
            URI failureUrl
    ) {
        User user = userRepository.findByPhone(authenticatedPhone)
                .filter(User::isPhoneVerified)
                .filter(foundUser -> !Boolean.TRUE.equals(foundUser.getIsDeleted()))
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден")
                );

        Booking booking = bookingRepository
                .findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() ->
                        new BookingNotFoundException("Бронирование не найдено")
                );

        Payment existingPayment = paymentRepository
                .findByMerchantOrderId(merchantOrderId)
                .orElse(null);

        if (existingPayment != null) {
            validateIdempotentRequest(
                    existingPayment,
                    booking,
                    providerCode
            );

            return toInitiationResult(existingPayment);
        }

        validateBooking(booking);

        Payment pendingPayment = paymentRepository
                .findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
                        booking.getId(),
                        PaymentStatus.PENDING
                )
                .orElse(null);

        if (pendingPayment != null) {
            validateIdempotentRequest(
                    pendingPayment,
                    booking,
                    providerCode
            );

            return toInitiationResult(pendingPayment);
        }

        PaymentProvider provider =
                providerRegistry.getRequired(providerCode);

        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .merchantOrderId(merchantOrderId)
                .provider(provider.getCode())
                .amount(booking.getAmount())
                .currency(CURRENCY_KGS)
                .status(PaymentStatus.PENDING)
                .refundedAmount(BigDecimal.ZERO)
                .build();

        PendingPaymentDecision decision = savePendingPayment(
                payment,
                booking,
                providerCode
        );

        payment = decision.payment();

        if (!decision.created()) {
            return toInitiationResult(payment);
        }

        try {
            CreatePaymentCommand command = new CreatePaymentCommand(
                    payment.getMerchantOrderId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    successUrl,
                    failureUrl
            );

            CreatePaymentResult providerResult =
                    provider.createPayment(command);

            applyProviderResult(payment, providerResult);

            Payment savedPayment = paymentRepository.save(payment);

            return toInitiationResult(savedPayment);

        } catch (PaymentProviderException ex) {
            markFailed(
                    payment,
                    ex.getProviderErrorCode(),
                    ex.getMessage()
            );

            throw ex;

        } catch (RuntimeException ex) {
            markFailed(
                    payment,
                    "UNEXPECTED_PROVIDER_ERROR",
                    ex.getMessage()
            );

            throw new PaymentProviderException(
                    "Unexpected payment provider error",
                    provider.getCode(),
                    "UNEXPECTED_PROVIDER_ERROR",
                    ex
            );
        }
    }

    private void validateBooking(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidPaymentStateException(
                    "Payment cannot be created for booking with status "
                            + booking.getStatus()
            );
        }

        if (booking.getAmount() == null
                || booking.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentStateException(
                    "Booking amount must be greater than zero"
            );
        }
    }

    private void validateIdempotentRequest(
            Payment payment,
            Booking booking,
            String providerCode
    ) {
        if (!payment.getBookingId().equals(booking.getId())) {
            throw new InvalidPaymentStateException(
                    "Idempotency key is already used for another operation"
            );
        }

        if (providerCode == null
                || providerCode.isBlank()
                || !payment.getProvider().equalsIgnoreCase(providerCode.trim())) {
            throw new InvalidPaymentStateException(
                    "Idempotency key is already used for another operation"
            );
        }

        if (payment.getAmount().compareTo(booking.getAmount()) != 0) {
            throw new InvalidPaymentStateException(
                    "Booking amount has changed after payment creation"
            );
        }

        if (!CURRENCY_KGS.equals(payment.getCurrency())) {
            throw new InvalidPaymentStateException(
                    "Existing payment has an unexpected currency"
            );
        }
    }

    private void applyProviderResult(
            Payment payment,
            CreatePaymentResult result
    ) {
        if (result == null
                || result.providerPaymentId() == null
                || result.paymentUrl() == null) {
            throw new PaymentProviderException(
                    "Provider returned an incomplete response",
                    payment.getProvider(),
                    "INVALID_PROVIDER_RESPONSE"
            );
        }

        payment.setProviderPaymentId(result.providerPaymentId());
        payment.setProviderStatus(result.providerStatus());
        payment.setPaymentUrl(result.paymentUrl().toString());
        payment.setExpiresAt(result.expiresAt());
    }

    private void markFailed(
            Payment payment,
            String failureCode,
            String failureMessage
    ) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureCode(failureCode);
        payment.setFailureMessage(limitFailureMessage(failureMessage));

        paymentRepository.save(payment);
    }

    private String limitFailureMessage(String message) {
        if (message == null) {
            return null;
        }

        int maximumLength = 1000;

        return message.length() <= maximumLength
                ? message
                : message.substring(0, maximumLength);
    }

    private PaymentInitiationResult toInitiationResult(
            Payment payment
    ) {
        URI paymentUrl = payment.getPaymentUrl() == null
                ? null
                : URI.create(payment.getPaymentUrl());

        return new PaymentInitiationResult(
                payment.getId(),
                payment.getBookingId(),
                payment.getStatus(),
                paymentUrl,
                payment.getExpiresAt()
        );
    }

    private PendingPaymentDecision savePendingPayment(
            Payment candidate,
            Booking booking,
            String providerCode
    ) {
        try {
            Payment saved = paymentRepository.saveAndFlush(candidate);
            return new PendingPaymentDecision(saved, true);

        } catch (DataIntegrityViolationException ex) {
            Payment existing = paymentRepository
                    .findByMerchantOrderId(candidate.getMerchantOrderId())
                    .orElseGet(() -> paymentRepository
                            .findFirstByBookingIdAndStatusOrderByCreatedAtDesc(
                                    booking.getId(),
                                    PaymentStatus.PENDING
                            )
                            .orElseThrow(() -> ex)
                    );

            validateIdempotentRequest(
                    existing,
                    booking,
                    providerCode
            );

            return new PendingPaymentDecision(existing, false);
        }
    }

    public PaymentInitiationResult getPayment(
            Long paymentId,
            String authenticatedPhone
    ) {
        User user = userRepository.findByPhone(authenticatedPhone)
                .filter(User::isPhoneVerified)
                .filter(foundUser ->
                        !Boolean.TRUE.equals(foundUser.getIsDeleted())
                )
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        Payment payment = paymentRepository
                .findByIdAndBookingUserId(
                        paymentId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found")
                );

        return toInitiationResult(payment);
    }

    private record PendingPaymentDecision(
            Payment payment,
            boolean created
    ) {
    }
}
