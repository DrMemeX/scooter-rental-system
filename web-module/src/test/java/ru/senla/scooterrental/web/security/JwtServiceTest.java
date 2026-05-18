package ru.senla.scooterrental.web.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_shouldGenerateSuccessfully() {
        String token = jwtService.generateToken(
                "ivan@example.com",
                "USER"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_shouldReturnEmailSuccessfully() {
        String token = jwtService.generateToken(
                "ivan@example.com",
                "USER"
        );

        String email = jwtService.extractEmail(token);

        assertEquals("ivan@example.com", email);
    }

    @Test
    void extractRole_shouldReturnRoleSuccessfully() {
        String token = jwtService.generateToken(
                "ivan@example.com",
                "ADMIN"
        );

        String role = jwtService.extractRole(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        String token = jwtService.generateToken(
                "ivan@example.com",
                "USER"
        );

        boolean result = jwtService.isTokenValid(token);

        assertTrue(result);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {
        boolean result = jwtService.isTokenValid("invalid.jwt.token");

        assertFalse(result);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsBlank() {
        boolean result = jwtService.isTokenValid("");

        assertFalse(result);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsNull() {
        boolean result = jwtService.isTokenValid(null);

        assertFalse(result);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired()
            throws Exception {

        String expiredToken = generateExpiredToken(
                "ivan@example.com",
                "USER"
        );

        boolean result = jwtService.isTokenValid(expiredToken);

        assertFalse(result);
    }

    @Test
    void extractEmail_shouldThrowException_whenTokenIsInvalid() {
        assertThrows(
                RuntimeException.class,
                () -> jwtService.extractEmail("invalid.jwt.token")
        );
    }

    @Test
    void extractRole_shouldThrowException_whenTokenIsInvalid() {
        assertThrows(
                RuntimeException.class,
                () -> jwtService.extractRole("invalid.jwt.token")
        );
    }

    @Test
    void extractEmail_shouldThrowException_whenTokenIsExpired()
            throws Exception {

        String expiredToken = generateExpiredToken(
                "ivan@example.com",
                "USER"
        );

        assertThrows(
                RuntimeException.class,
                () -> jwtService.extractEmail(expiredToken)
        );
    }

    @Test
    void extractRole_shouldThrowException_whenTokenIsExpired()
            throws Exception {

        String expiredToken = generateExpiredToken(
                "ivan@example.com",
                "USER"
        );

        assertThrows(
                RuntimeException.class,
                () -> jwtService.extractRole(expiredToken)
        );
    }

    @Test
    void generateToken_shouldAllowDifferentRoles() {
        String userToken = jwtService.generateToken(
                "user@example.com",
                "USER"
        );
        String adminToken = jwtService.generateToken(
                "admin@example.com",
                "ADMIN"
        );

        assertEquals("USER", jwtService.extractRole(userToken));
        assertEquals("ADMIN", jwtService.extractRole(adminToken));
    }

    @Test
    void generateToken_shouldAllowDifferentEmails() {
        String firstToken = jwtService.generateToken(
                "first@example.com",
                "USER"
        );
        String secondToken = jwtService.generateToken(
                "second@example.com",
                "USER"
        );

        assertEquals("first@example.com", jwtService.extractEmail(firstToken));
        assertEquals("second@example.com", jwtService.extractEmail(secondToken));
    }

    @Test
    void generatedToken_shouldContainEmailAndRole() {
        String token = jwtService.generateToken(
                "ivan@example.com",
                "ADMIN"
        );

        assertDoesNotThrow(() -> jwtService.extractEmail(token));
        assertDoesNotThrow(() -> jwtService.extractRole(token));

        assertEquals("ivan@example.com", jwtService.extractEmail(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    private String generateExpiredToken(String email,
                                        String role)
            throws Exception {

        SecretKey key = extractPrivateKey();

        Date issuedAt = new Date(System.currentTimeMillis() - 2000);
        Date expiration = new Date(System.currentTimeMillis() - 1000);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey extractPrivateKey() throws Exception {
        Field keyField = JwtService.class.getDeclaredField("key");
        keyField.setAccessible(true);

        return (SecretKey) keyField.get(jwtService);
    }
}