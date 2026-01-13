package com.quantumsave.quantum_save.security;

import com.quantumsave.quantum_save.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ✅ Let preflight through (no auth)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Request correlation id (use existing if you already pass one from FE/gateway)
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put("rid", requestId);

        try {
            final String path = request.getRequestURI();
            final String method = request.getMethod();

            // Keep this at DEBUG only (too noisy for INFO in prod)
            if (log.isDebugEnabled()) {
                log.debug("JWT filter: {} {}", method, path);
            }

            // Already authenticated => continue
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Read auth header (no need to check lowercase separately; servlet containers are case-insensitive)
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            // No bearer token => continue (Security will block protected routes)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);

            final String email;
            try {
                email = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Don’t leak token or user data. Just state “bad token”.
                log.warn("JWT rejected: cannot parse token");
                filterChain.doFilter(request, response);
                return;
            }

            if (email == null || email.isBlank()) {
                log.warn("JWT rejected: missing subject");
                filterChain.doFilter(request, response);
                return;
            }

            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                boolean valid = jwtUtil.validateToken(jwt, userDetails);
                if (!valid) {
                    log.warn("JWT rejected: invalid/expired token");
                    filterChain.doFilter(request, response);
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // ✅ Optional: confirm auth was set without identifying the user
                if (log.isDebugEnabled()) {
                    log.debug("JWT accepted: security context authenticated");
                }

            } catch (Exception e) {
                // Don’t log email. Don’t log stacktrace unless debug.
                log.warn("JWT rejected: auth setup failed");
                if (log.isDebugEnabled()) {
                    log.debug("JWT auth setup failure details", e);
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove("rid");
        }
    }
}
