package kg.megalab.pivnitsabackend.entity;

import jakarta.persistence.*;
import kg.megalab.pivnitsabackend.otp.OtpPurpose;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 30)
    private String phone;


    @Column(nullable = false, length = 6)
    private String code;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;


    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;


    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;


    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;


    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;


    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (createdAt == null) {
            createdAt = now;
        }

        if (sentAt == null) {
            sentAt = now;
        }

        if (expiresAt == null) {
            expiresAt = now.plusMinutes(5);
        }

        if (failedAttempts == null) {
            failedAttempts = 0;
        }
    }
}