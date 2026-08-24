package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByUserIdOrderByCreatedAtDescIdDesc(
            Long userId,
            Pageable pageable
    );

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query(
            value = """
                INSERT INTO notifications (
                    user_id,
                    type,
                    title,
                    body,
                    target_type,
                    target_id,
                    deduplication_key
                )
                VALUES (
                    :userId,
                    :type,
                    :title,
                    :body,
                    :targetType,
                    :targetId,
                    :deduplicationKey
                )
                ON CONFLICT (user_id, deduplication_key)
                DO NOTHING
                """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("title") String title,
            @Param("body") String body,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("deduplicationKey") String deduplicationKey
    );
}
