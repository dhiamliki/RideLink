package com.ridelink.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final AuthProperties props;

    public JwtService(AuthProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(UUID userId, String phoneNumber) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("phoneNumber", phoneNumber)
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(now.plus(props.getJwt().getAccessTtl())))
                .signWith(key)
                .compact();
    }

    // Returns the userId (subject) if the token is a valid, unexpired signature; null otherwise.
    public UUID parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
