package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByPhoneOrderByCreatedAtDesc(String phone);

    boolean existsByPhoneAndSentAtAfter(
            String phone,
            OffsetDateTime time
    );
}