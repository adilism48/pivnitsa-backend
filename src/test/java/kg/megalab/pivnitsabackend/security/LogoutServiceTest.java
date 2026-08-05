package kg.megalab.pivnitsabackend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    @DisplayName("Должен успешно добавить токен в черный список при валидном заголовке")
    void logout_Success() {
        // Arrange
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;
        String jti = "test-jti-uuid";
        Instant expiration = Instant.now().plusSeconds(3600);

        when(jwtService.extractJti(token)).thenReturn(jti);
        when(jwtService.extractClaim(eq(token), any())).thenReturn(Date.from(expiration));

        // Act
        logoutService.logout(authHeader);

        // Assert
        verify(tokenBlacklistService, times(1)).blacklist(eq(jti),
                argThat(exp -> Math.abs(exp.toEpochMilli() - expiration.toEpochMilli()) < 1000));
    }

    @Test
    @DisplayName("Не должен ничего делать, если заголовок null или не начинается с Bearer")
    void logout_InvalidHeader_ShouldDoNothing() {
        logoutService.logout(null);
        logoutService.logout("InvalidHeaderFormat");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenBlacklistService);
    }
}
