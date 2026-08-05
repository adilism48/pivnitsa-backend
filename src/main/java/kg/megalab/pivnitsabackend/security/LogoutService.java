package kg.megalab.pivnitsabackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7);

        try {
            String jti = jwtService.extractJti(token);
            Instant expiration = jwtService.extractClaim(token, Claims::getExpiration).toInstant();

            log.debug("Blacklisting token with JTI: {} expiring at: {}", jti, expiration);

            tokenBlacklistService.blacklist(jti, expiration);

        } catch (JwtException | IllegalArgumentException e) {
            log.trace("Invalid or expired token during logout: {}", e.getMessage());
        }
    }
}
