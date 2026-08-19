package com.flooring.finance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Issues and validates the JWT used for the single owner session. Kept
 * deliberately small: one subject claim (username), one expiry.
 */
@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, int tokenVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(now)
                .expiration(expiry)
                .signWith((SecretKey) signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * A token minted before this claim existed (i.e. still sitting in a
     * browser from before this feature was deployed) simply has no
     * "tokenVersion" claim - reading it returns null, and unboxing that
     * straight into a primitive int used to throw a NullPointerException
     * inside the security filter chain, which broke every request from that
     * browser, including /api/auth/login itself. -1 never matches a real
     * user's version (always >= 0), so an old-format token is just treated
     * as stale/logged-out like any other invalid token, instead of crashing.
     */
    public int extractTokenVersion(String token) {
        Integer version = parseClaims(token).get("tokenVersion", Integer.class);
        return version != null ? version : -1;
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
