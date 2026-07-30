package kg.megalab.pivnitsabackend.otp;

import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NikitaKgOtpSender implements OtpSender {

    @Value("${otp.nikita.enabled:false}")
    private boolean enabled;

    @Value("${otp.nikita.login:}")
    private String login;

    @Value("${otp.nikita.password:}")
    private String password;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS;
    }

    @Override
    public void send(String phone, String code) {
        if (!enabled) {
            log.info("[nikita.kg STUB] Код для {}: {}", phone, code);
            return;
        }
        try {
            // TODO: реальный HTTP-вызов nikita.kg API, когда получим боевые login/password
        } catch (Exception e) {
            throw new OtpSendException("nikita.kg OTP send failed", e);
        }
    }
}