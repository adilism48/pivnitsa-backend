package kg.megalab.pivnitsabackend.security;

import kg.megalab.pivnitsabackend.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TokenBlacklistService blacklistService;

    @Test
    @DisplayName("Должен сохранять JTI в Redis и подтверждать его наличие в черном списке")
    void shouldBlacklistTokenInRedis() {
        // Arrange
        String jti = "unique-jti-12345";
        Instant expiration = Instant.now().plusSeconds(60);

        // Act
        blacklistService.blacklist(jti, expiration);

        // Assert
        boolean isBlacklisted = blacklistService.isBlacklisted(jti);
        assertTrue(isBlacklisted, "Токен должен присутствовать в черном списке Redis");
    }
}
