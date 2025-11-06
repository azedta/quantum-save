package com.quantumsave.quantum_save.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;            // <-- important import
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")     // BASE64-encoded 32+ byte secret
    private String secretBase64;

    @Value("${jwt.expiration}") // e.g., 86400000 (24h)
    private long expirationMs;

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes); // returns SecretKey
    }

    /* --------- Create token --------- */
    public String generateToken(String subject) {
        return generateToken(Map.of(), subject);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())    // algorithm inferred from key
                .compact();
    }

    /* --------- Parse / extract --------- */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parseClaims(token);
        return resolver.apply(claims);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())      // expects SecretKey
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // still readable after expiry
        }
    }

    /* --------- Validate --------- */
    public boolean isTokenValid(String token, String expectedUsername) {
        String username = extractUsername(token);
        return username.equals(expectedUsername) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            // Parse & verify signature (will throw if signature invalid or token malformed).
            var jws = Jwts.parser()
                    .verifyWith(signingKey())   // SecretKey
                    .build()
                    .parseSignedClaims(token);

            var claims = jws.getPayload();
            String username = claims.getSubject();

            // Must belong to the same user and not be expired
            return username != null
                    && username.equals(userDetails.getUsername())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (ExpiredJwtException e) {
            // Token is expired
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid signature, malformed, unsupported, etc.
            return false;
        }
    }
}