package uk.ac.rhul.cs3821.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private static final String SECRET = "change-this-to-a-very-very-long-secret-key-change";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final long EXP = 1000L * 60 * 60 * 24; // 24h

    public String generate(String subject, Map<String, Object> claims) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXP))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).getBody().getSubject();
    }
}
