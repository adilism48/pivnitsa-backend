package kg.megalab.pivnitsabackend.service;

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
    public User findOrCreateUser(String phone) {
        return userRepository.findByPhone(phone).orElseGet(() -> createUser(phone));
    }

    private User createUser(String phone) {
        User user = User.builder()
                .phone(phone)
                .firstName("")
                .lastName("")
                .build();
        return userRepository.save(user);
    }
}
