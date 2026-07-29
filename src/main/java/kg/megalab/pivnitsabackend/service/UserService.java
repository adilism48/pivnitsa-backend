package kg.megalab.pivnitsabackend.service;

import kg.megalab.pivnitsabackend.dto.CompleteProfileRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User completeProfile(String phone, CompleteProfileRequest request) {
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