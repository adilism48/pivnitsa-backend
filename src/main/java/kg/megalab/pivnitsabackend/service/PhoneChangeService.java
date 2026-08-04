package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.UserResponse;
import kg.megalab.pivnitsabackend.dto.phone.ConfirmPhoneChangeRequest;
import kg.megalab.pivnitsabackend.dto.phone.PhoneChangeResponse;
import kg.megalab.pivnitsabackend.dto.phone.SendPhoneChangeOtpRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.PhoneAlreadyUsedException;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.otp.OtpPurpose;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import kg.megalab.pivnitsabackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhoneChangeService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public void sendOtp(
            String currentPhone,
            SendPhoneChangeOtpRequest request
    ) {
        User user = findVerifiedUser(currentPhone);
        String newPhone = normalize(request.newPhone());

        if (user.getPhone().equals(newPhone)) {
            throw new IllegalArgumentException(
                    "Новый номер совпадает с текущим"
            );
        }

        if (userRepository.existsByPhone(newPhone)) {
            throw new PhoneAlreadyUsedException(
                    "Этот номер уже используется"
            );
        }

        otpService.sendOtp(
                newPhone,
                request.channel(),
                OtpPurpose.PHONE_CHANGE
        );
    }

    private User findVerifiedUser(String phone) {
        return userRepository.findByPhone(phone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Пользователь не найден"
                        )
                );
    }

    private String normalize(String phone) {
        return phone.replaceAll("[\\s()-]", "");
    }

    @Transactional
    public PhoneChangeResponse confirm(
            String currentPhone,
            ConfirmPhoneChangeRequest request
    ) {
        User user = findVerifiedUser(currentPhone);
        String newPhone = normalize(request.newPhone());

        if (user.getPhone().equals(newPhone)) {
            throw new IllegalArgumentException(
                    "Новый номер совпадает с текущим"
            );
        }

        if (userRepository.existsByPhone(newPhone)) {
            throw new PhoneAlreadyUsedException(
                    "Этот номер уже используется"
            );
        }

        otpService.verifyOtp(
                newPhone,
                request.code(),
                OtpPurpose.PHONE_CHANGE
        );

        user.setPhone(newPhone);
        user.setPhoneVerified(true);

        User savedUser = userRepository.save(user);

        String newToken = jwtService.generateAccessToken(newPhone);

        return new PhoneChangeResponse(
                newToken,
                "Bearer",
                new UserResponse(
                        savedUser.getId(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        savedUser.getPhone(),
                        savedUser.getEmail()
                )
        );
    }
}
