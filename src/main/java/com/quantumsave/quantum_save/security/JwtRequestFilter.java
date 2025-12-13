package com.quantumsave.quantum_save.security;

import com.quantumsave.quantum_save.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

        // ✅ Let preflight through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String path = request.getRequestURI();
        log.info("JwtRequestFilter hit: {}", path);

        // ✅ Try both header casings
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) authHeader = request.getHeader("authorization");

        // No bearer token => continue (security will block protected routes)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String email;

        try {
            email = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            log.warn("JWT parse failed: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Already authenticated => continue
        if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            boolean valid = jwtUtil.validateToken(jwt, userDetails);

            if (valid) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // ✅ This is the most important confirmation log
                log.info("JWT OK → authenticated user: {}", userDetails.getUsername());
            } else {
                log.warn("JWT invalid for user: {}", email);
            }

        } catch (Exception e) {
            log.warn("Auth setup failed for {}: {}", email, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
