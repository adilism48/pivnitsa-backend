package kg.megalab.pivnitsabackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.megalab.pivnitsabackend.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            final String jti = jwtService.extractJti(token);
            final String phone = jwtService.extractPhone(token);
            final String scope = jwtService.extractScope(token);

            if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (tokenBlacklistService.isBlacklisted(jti)) {
                    throw new InvalidTokenException("Невалидный или просроченный токен");
                }

                if (jwtService.isTokenValid(token, phone)) {
                    List<GrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + scope));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    phone,
                                    null,
                                    authorities
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (InvalidTokenException e) {
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            log.debug("Невалидный или просроченный токен для запроса {}: {}",
                    request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();

            handlerExceptionResolver.resolveException(request, response, null, new InvalidTokenException("Невалидный или просроченный токен"));
        }
    }
}