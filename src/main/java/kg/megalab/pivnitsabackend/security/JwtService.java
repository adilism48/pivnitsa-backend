package kg.megalab.pivnitsabackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {
    public static final String SCOPE_CLAIM = "scope";
    public static final String SCOPE_PRE_AUTH = "PRE_AUTH";
    public static final String SCOPE_FULL_ACCESS = "FULL_ACCESS";
    private static final long PRE_AUTH_TTL_MILLIS = 10 * 60 * 1000;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generatePreAuthToken(String phone) {
        return buildToken(phone, SCOPE_PRE_AUTH, PRE_AUTH_TTL_MILLIS);
    }

    public String generateAccessToken(String phone) {
        return buildToken(phone, SCOPE_FULL_ACCESS, jwtExpiration);
    }

    private String buildToken(String phone, String scope, long ttlMillis) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(phone)
                .claim(SCOPE_CLAIM, scope)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractPhone(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractScope(String token) {
        return extractClaim(token, claims -> claims.get(SCOPE_CLAIM, String.class));
    }

    public String extractJti(String token) {
        return extractClaim(token, claims -> claims.get("jti", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String phone) {
        final String extractedPhone = extractPhone(token);
        return extractedPhone.equals(phone) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}