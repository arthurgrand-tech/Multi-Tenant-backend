package com.ArthurGrand.common.component;

import com.ArthurGrand.module.employee.entity.Employee;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Employee employee) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeId", employee.getEmployeeId());
        claims.put("email", employee.getEmailid());
        claims.put("firstName", employee.getFirstname());
        claims.put("lastName", employee.getLastname());

        return createToken(claims, employee.getEmailid());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.err.println("JWT token is malformed: " + e.getMessage());
            throw e;
        } catch (SecurityException e) {
            System.err.println("JWT signature validation failed: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            System.err.println("JWT token compact of handler are invalid: " + e.getMessage());
            throw e;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Integer extractEmployeeId(String token) {
        return extractClaim(token, claims -> claims.get("employeeId", Integer.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public Boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    // Utility method to extract claims safely
    public Map<String, Object> extractAllClaims(String token) {
        try {
            Claims claims = parseToken(token);
            return new HashMap<>(claims);
        } catch (Exception e) {
            System.err.println("Failed to extract claims: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Method to check if token needs refresh (expires within 5 minutes)
    public Boolean shouldRefreshToken(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            long timeUntilExpiration = expiration.getTime() - now.getTime();
            return timeUntilExpiration < 5 * 60 * 1000; // 5 minutes in milliseconds
        } catch (Exception e) {
            return true; // If we can't determine, assume it should be refreshed
        }
    }

    // Method to get remaining token validity time in seconds
    public Long getRemainingValidityInSeconds(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            long remaining = (expiration.getTime() - now.getTime()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0L;
        }
    }
}