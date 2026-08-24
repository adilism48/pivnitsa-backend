package kg.megalab.pivnitsabackend.service.notifications;

import kg.megalab.pivnitsabackend.dto.notification.NotificationPageResponse;
import kg.megalab.pivnitsabackend.dto.notification.NotificationResponse;
import kg.megalab.pivnitsabackend.entity.Notification;
import kg.megalab.pivnitsabackend.entity.NotificationTargetType;
import kg.megalab.pivnitsabackend.entity.NotificationType;
import kg.megalab.pivnitsabackend.entity.User;
import kg.megalab.pivnitsabackend.exception.NotificationNotFoundException;
import kg.megalab.pivnitsabackend.exception.UserNotFoundException;
import kg.megalab.pivnitsabackend.repository.NotificationRepository;
import kg.megalab.pivnitsabackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public NotificationPageResponse getCurrentUserNotifications(String phone, int page, int size) {
        User user = getVerifiedUser(phone);

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(
                Math.max(size, 1),
                100
        );

        PageRequest pageable = PageRequest.of(
                normalizedPage,
                normalizedSize
        );

        Page<Notification> notificationPage =
                notificationRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(
                        user.getId(),
                        pageable
                );

        List<NotificationResponse> items = notificationPage
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(
                user.getId()
        );

        return new NotificationPageResponse(
                items,
                normalizedPage,
                normalizedSize,
                notificationPage.hasNext(),
                unreadCount
        );
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String phone, Long notificationId) {
        User user = getVerifiedUser(phone);

        Notification notification = notificationRepository
                .findByIdAndUserId(
                        notificationId,
                        user.getId()
                ).orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Уведомление не найдено"
                        ));

        notification.markAsRead();

        Notification savedNotification = notificationRepository.save(notification);

        return toResponse(savedNotification);
    }

    @Override
    @Transactional
    public void createForUsers(
            List<Long> userIds,
            NotificationType type,
            String title,
            String body,
            NotificationTargetType targetType,
            Long targetId,
            String deduplicationKey
    ) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        Objects.requireNonNull(type, "Notification type is required");
        Objects.requireNonNull(title, "Notification title is required");
        Objects.requireNonNull(body, "Notification body is required");
        Objects.requireNonNull(
                deduplicationKey,
                "Deduplication key is required"
        );

        boolean onlyOneTargetValueProvided =
                (targetType == null) != (targetId == null);

        if (onlyOneTargetValueProvided) {
            throw new IllegalArgumentException(
                    "Target type and target ID must be provided together"
            );
        }

        String targetTypeValue =
                targetType == null ? null : targetType.name();

        userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(userId ->
                        notificationRepository.insertIfAbsent(
                                userId,
                                type.name(),
                                title,
                                body,
                                targetTypeValue,
                                targetId,
                                deduplicationKey
                        )
                );

    }

    private User getVerifiedUser(String phone){
        return userRepository.findByPhone(phone)
                .filter(User::isPhoneVerified)
                .orElseThrow(() ->
                    new UserNotFoundException("Пользователь не найден")
                );
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
