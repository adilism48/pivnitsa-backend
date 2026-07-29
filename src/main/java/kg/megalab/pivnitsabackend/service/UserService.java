package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.CompleteProfileRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import kg.megalab.pivnitsabackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Transactional
    public User completeProfile(String authHeader, CompleteProfileRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Неверный формат заголовка Authorization");
        }

        String token = authHeader.substring(7);
        String phone = jwtService.extractPhone(token);

        return userRepository.findByPhone(phone)
                .map(existingUser -> {
                    existingUser.setFirstName(request.firstName());
                    existingUser.setLastName(request.lastName());
                    existingUser.setPhoneVerified(true);
                    existingUser.setTermsAccepted(request.termsAccepted());
                    existingUser.setPrivacyAccepted(request.privacyAccepted());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .phone(phone)
                            .firstName(request.firstName())
                            .lastName(request.lastName())
                            .phoneVerified(true)
                            .termsAccepted(request.termsAccepted())
                            .privacyAccepted(request.privacyAccepted())
                            .build();
                    return userRepository.save(newUser);
                });
    }
}