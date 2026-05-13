package ru.senla.scooterrental.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log =
            LoggerFactory.getLogger(JwtService.class);

    private static final String SECRET =
            "superSecretKeyForScooterRentalSystemJwt123456789";

    private static final long EXPIRATION =
            1000 * 60 * 60 * 24;

    private final SecretKey key = Keys.hmacShaKeyFor(
            SECRET.getBytes(StandardCharsets.UTF_8)
    );

    public String generateToken(String email,
                                String role) {

        log.info(
                "Generating JWT token: email={}, role={}",
                email,
                role
        );

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        String token = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info(
                "JWT token generated successfully: email={}, role={}",
                email,
                role
        );

        return token;
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token)
                .get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);

            log.info("JWT token validated successfully");

            return true;
        } catch (Exception ex) {

            log.warn(
                    "JWT token validation failed: {}",
                    ex.getMessage()
            );

            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}