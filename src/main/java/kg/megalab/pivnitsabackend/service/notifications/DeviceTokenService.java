package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.dto.notification.RegisterTokenRequest;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.entity.UserDeviceToken;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.DeviceTokenRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public List<String> getTokensByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return deviceTokenRepository.findAllTokensByUserIds(userIds);
    }

    @Transactional
    public void saveOrUpdateTokenByPhone(String phone, RegisterTokenRequest request) {
        User user = getVerifiedUser(phone);

        saveOrUpdateToken(user.getId(), request);
    }

    @Transactional
    public void saveOrUpdateToken(Long userId, RegisterTokenRequest request) {
        deviceTokenRepository.findByToken(request.token())
                .ifPresentOrElse(
                        existing -> {
                            existing.setUserId(userId);
                            existing.setDeviceType(request.deviceType());
                        },
                        () -> deviceTokenRepository.save(UserDeviceToken.builder()
                                .userId(userId)
                                .token(request.token())
                                .deviceType(request.deviceType())
                                .build())
                );
    }

    @Transactional
    public void removeToken(String phone, String token) {
        User user = getVerifiedUser(phone);

        int deleted = deviceTokenRepository.deleteByTokenAndUserId(token, user.getId());

        if (deleted == 0) {
            log.warn("Попытка удаления несуществующего/чужого токена, userId={}", user.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeInvalidTokens(List<String> invalidTokens) {
        if (invalidTokens != null && !invalidTokens.isEmpty()) {
            log.info("Удаление {} невалидных FCM токенов из БД...", invalidTokens.size());
            deviceTokenRepository.deleteByTokenIn(invalidTokens);
            log.info("Токены успешно удалены из БД");
        }
    }

    private User getVerifiedUser(String phone) {
        return userRepository.findByPhone(phone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                        new UserNotFoundException("Пользователь не найден")
                );
    }
}
