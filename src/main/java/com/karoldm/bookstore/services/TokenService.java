package com.karoldm.bookstore.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    private final String secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public TokenService(
            @Value("${api.security.token.secret}") String secretKey,
            @Value("${api.security.token.expiration}") long jwtExpiration,
            @Value("${api.security.token.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(String username) {
        return buildToken(username, jwtExpiration);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, refreshExpiration);
    }

    private String buildToken(String username, long expirationHour) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            return JWT.create().withSubject(username)
                    .withExpiresAt(generateExpirationDate(expirationHour)).sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token: " + exception);
        }
    }

    public String validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        return JWT.require(algorithm).build().verify(token).getSubject();
    }

    private Instant generateExpirationDate(long expirationHour) {
        return OffsetDateTime.now(ZoneOffset.UTC).plusHours(expirationHour).toInstant();
    }
}
