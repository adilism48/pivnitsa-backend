package kg.megalab.pivnitsabackend.otp;

import kg.megalab.pivnitsabackend.entity.NotificationChannel;

public interface OtpSender {
    boolean supports(NotificationChannel channel);
    void send(String phone, String code);
}