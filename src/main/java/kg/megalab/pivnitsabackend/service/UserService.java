package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.profile.CompleteProfileRequest;
import kg.megalab.pivnitsabackend.dto.profile.UpdateProfileRequest;
import kg.megalab.pivnitsabackend.dto.UserResponse;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import kg.megalab.pivnitsabackend.exception.PhoneAlreadyUsedException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User completeProfile(String phone, CompleteProfileRequest request) {
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new PhoneAlreadyUsedException(
                    "Этот номер телефона уже зарегистрирован. Пожалуйста, войдите в аккаунт."
            );
        }

        User newUser = User.builder()
                .phone(phone)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneVerified(true)
                .termsAccepted(request.termsAccepted())
                .privacyAccepted(request.privacyAccepted())
                .build();

        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new PhoneAlreadyUsedException(
                    "Этот номер телефона уже зарегистрирован. Пожалуйста, войдите в аккаунт."
            );
        }
    }

    @Transactional
    public UserResponse updateCurrentUser(
            String currentPhone,
            UpdateProfileRequest request
    ) {
        User user = userRepository.findByPhone(currentPhone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден")
                );

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(normalizeEmail(request.email()));

        return toResponse(user);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail()
        );
    }

    public UserResponse getCurrentUser(String phone) {
        User user = userRepository.findByPhone(phone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден")
                );

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail()
        );
    }
}