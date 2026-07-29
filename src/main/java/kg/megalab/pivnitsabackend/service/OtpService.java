package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.exception.InvalidOtpException;
import kg.megalab.pivnitsabackend.exception.OtpAlreadySentException;
import kg.megalab.pivnitsabackend.exception.OtpExpiredException;
import kg.megalab.pivnitsabackend.exception.TooManyAttemptsException;
import org.springframework.transaction.annotation.Transactional;
import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import kg.megalab.pivnitsabackend.entity.OtpCode;
import kg.megalab.pivnitsabackend.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class OtpService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final OtpCodeRepository otpCodeRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional(noRollbackFor = {
            InvalidOtpException.class,
            TooManyAttemptsException.class,
            OtpExpiredException.class
    })
    public void verifyOtp(String phone, String code) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        OtpCode otpCode = otpCodeRepository.findTopByPhoneOrderByCreatedAtDesc(phone)
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

    @Transactional
    public void sendOtp(String phone) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        boolean recentlySent = otpCodeRepository.existsByPhoneAndSentAtAfter(phone, now.minusMinutes(1));

        if(recentlySent) {
            throw new OtpAlreadySentException("Код уже отправлен. Повторите попытку через 1 минуту.");
        }

        String code = generateOtp();

        OtpCode otpCode = OtpCode.builder()
                .phone(phone)
                .code(code)
                .channel(NotificationChannel.SMS)
                .sentAt(now)
                .expiresAt(now.plusMinutes(5))
                .build();

        otpCodeRepository.save(otpCode);

        System.out.println("OTP code for " + phone + ": " + code);
    }

    private String generateOtp() {
        int number = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(number);
    }
}
