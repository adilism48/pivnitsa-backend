package kg.megalab.pivnitsabackend.otp;

import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(2)
@Component
public class InfobipOtpSender implements OtpSender {

    @Value("${otp.infobip.enabled:false}")
    private boolean enabled;

    @Value("${otp.infobip.api-key:}")
    private String apiKey;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS
                || channel == NotificationChannel.WHATSAPP
                || channel == NotificationChannel.EMAIL;
    }

    @Override
    public void send(String phone, String code) {
        if (!enabled) {
            log.info("[Infobip STUB] Код для {}: {}", phone, code);
            return;
        }
        try {
        } catch (Exception e) {
            throw new OtpSendException("Infobip OTP send failed", e);
        }
    }
}