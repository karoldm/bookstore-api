package com.karoldm.bookstore.services;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenServiceTest {
    private TokenService tokenService;

    final private String username = "karol.marques";

    @BeforeEach
    void setUp() throws Exception {
        tokenService = new TokenService(
                "secret-key",
                2,
                168
        );
    }

    @Test
    void shouldGenerateTokenWithSubject() {
        String token = tokenService.generateToken(username);
        String subject = tokenService.validateToken(token);

        assertEquals("karol.marques", subject);

    }

    @Test
    void shouldGenerateRefreshTokenWithSubject() {
        String token = tokenService.generateRefreshToken(username);
        String subject = tokenService.validateToken(token);

        assertEquals("karol.marques", subject);
    }

    @Test
    void shouldThrowJWTVerificationExceptionWhenGenerateToken() throws NoSuchFieldException, IllegalAccessException {
        tokenService = new TokenService(
                "secret-key",
                0,
                168
        );

        String token = tokenService.generateToken(username);

        Exception exception = assertThrows(JWTVerificationException.class, () -> {
            tokenService.validateToken(token);
        });
    }

    @Test
    void shouldthrowJWTVerificationExceptionWhenGenerateRefreshToken() throws NoSuchFieldException, IllegalAccessException {
        tokenService = new TokenService(
                "secret-key",
                2,
                0
        );

        String token = tokenService.generateRefreshToken(username);

        Exception exception = assertThrows(JWTVerificationException.class, () -> {
            tokenService.validateToken(token);
        });
    }
}
