package kg.megalab.pivnitsabackend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        TokenBlacklistService.class
})
@Import(DataRedisAutoConfiguration.class)
@Testcontainers
class TokenBlacklistServiceIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

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
