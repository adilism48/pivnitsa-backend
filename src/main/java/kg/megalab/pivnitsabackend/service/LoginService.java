package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.LoginResponse;
import kg.megalab.pivnitsabackend.dto.UserResponse;
import kg.megalab.pivnitsabackend.entity.NotificationChannel;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.otp.OtpPurpose;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import kg.megalab.pivnitsabackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public void sendOtp(
            String phone,
            NotificationChannel channel
    ) {
        User user = userRepository.findByPhone(phone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Аккаунт с таким номером не найден"
                        )

                );

        otpService.sendOtp(
                user.getPhone(),
                channel,
                OtpPurpose.LOGIN
        );
    }

    public LoginResponse verifyOtp(
            String phone,
            String code
    ) {
        User user = userRepository.findByPhone(phone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Аккаунт с таким номером не найден"
                        )
                );

        otpService.verifyOtp(
                phone,
                code,
                OtpPurpose.LOGIN
        );

        String accessToken = jwtService.generateAccessToken(phone);

        return new LoginResponse(
                accessToken,
                "Bearer",
                "AUTHENTICATED",
                new UserResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhone()
                )
        );
    }
}
