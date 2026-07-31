package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import kg.megalab.pivnitsabackend.entity.OtpCode;
import kg.megalab.pivnitsabackend.exception.InvalidOtpException;
import kg.megalab.pivnitsabackend.exception.OtpAlreadySentException;
import kg.megalab.pivnitsabackend.exception.OtpExpiredException;
import kg.megalab.pivnitsabackend.exception.TooManyAttemptsException;
import kg.megalab.pivnitsabackend.otp.OtpDispatcherService;
import kg.megalab.pivnitsabackend.otp.OtpPurpose;
import kg.megalab.pivnitsabackend.repository.OtpCodeRepository;
import kg.megalab.pivnitsabackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long COOLDOWN_SECONDS = 60;

    private final OtpCodeRepository otpCodeRepository;
    private final OtpDispatcherService otpDispatcherService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void sendOtp(String phone, NotificationChannel channel, OtpPurpose purpose) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        otpCodeRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose)
                .filter(last -> last.getSentAt().isAfter(now.minusSeconds(COOLDOWN_SECONDS)))
                .ifPresent(last -> {
                    long secondsLeft = COOLDOWN_SECONDS
                            - Duration.between(last.getSentAt(), now).getSeconds();

                    throw new OtpAlreadySentException(
                            "Повторная отправка будет доступна через " + secondsLeft + " сек.",
                            Math.max(secondsLeft, 1)
                    );
                });

        String code = generateOtp();

        OtpCode otpCode = OtpCode.builder()
                .phone(phone)
                .code(code)
                .channel(channel)
                .purpose(purpose)
                .sentAt(now)
                .expiresAt(now.plusMinutes(5))
                .build();

        otpCodeRepository.save(otpCode);

        otpDispatcherService.dispatch(phone, code, channel);
    }

    @Transactional(noRollbackFor = {
            InvalidOtpException.class,
            TooManyAttemptsException.class,
            OtpExpiredException.class
    })
    public void verifyOtp(String phone, String code, OtpPurpose purpose) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        OtpCode otpCode = otpCodeRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose)
                .orElseThrow(() -> new InvalidOtpException("Код не найден"));

        if (otpCode.isVerified()) {
            throw new InvalidOtpException("Код уже был использован");
        }

        if (otpCode.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            throw new TooManyAttemptsException("Превышено количество попыток. Запросите новый код.");
        }

        if (otpCode.getExpiresAt().isBefore(now)) {
            throw new OtpExpiredException("Срок действия кода истёк");
        }

        if (!otpCode.getCode().equals(code)) {
            int newFailedAttempts = otpCode.getFailedAttempts() + 1;
            otpCode.setFailedAttempts(newFailedAttempts);
            otpCodeRepository.save(otpCode);

            if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
                throw new TooManyAttemptsException("Превышено количество попыток. Запросите новый код.");
            }

            throw new InvalidOtpException("Неверный код");
        }

        otpCode.setVerified(true);
        otpCodeRepository.save(otpCode);
    }

    private String generateOtp() {
        int number = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(number);
    }

}