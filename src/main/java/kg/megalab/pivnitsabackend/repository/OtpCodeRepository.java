package kg.megalab.pivnitsabackend.repository;

import kg.megalab.pivnitsabackend.entity.OtpCode;
import kg.megalab.pivnitsabackend.otp.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode>
    findTopByPhoneAndPurposeOrderByCreatedAtDesc(
            String phone,
            OtpPurpose purpose
    );

    boolean existsByPhoneAndPurposeAndSentAtAfter(
            String phone,
            OtpPurpose purpose,
            OffsetDateTime after
    );
}