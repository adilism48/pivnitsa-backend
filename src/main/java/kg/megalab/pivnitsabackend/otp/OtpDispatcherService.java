package kg.megalab.pivnitsabackend.otp;

import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpDispatcherService {

    private final List<OtpSender> senders;

    public void dispatch(String phone, String code, NotificationChannel channel) {
        List<OtpSender> candidates = senders.stream()
                .filter(sender -> sender.supports(channel))
                .toList();

        if (candidates.isEmpty()) {
            throw new OtpSendException("Нет доступного провайдера для канала " + channel, null);
        }

        RuntimeException lastError = null;
        for (OtpSender sender : candidates) {
            try {
                sender.send(phone, code);
                return;
            } catch (OtpSendException e) {
                log.warn("Провайдер {} не смог отправить OTP на {}: {}",
                        sender.getClass().getSimpleName(), phone, e.getMessage());
                lastError = e;
            }
        }
        throw lastError;
    }
}