package com.planb.supportticket.security.jwt;

import com.planb.supportticket.config.JwtConfig;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provider for JWT token generation and validation.
 */
@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final Key key;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        // Generate a secure key for HS512 algorithm
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param userProfile the user profile
     * @return the JWT token
     */
    public String generateToken(UserProfile userProfile) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        // Convert roles to Spring Security role names
        List<String> roles = userProfile.getRoles().stream()
                .map(role -> "ROLE_" + role.name())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userProfile.getId().toString())
                .claim("email", userProfile.getEmail())
                .claim("name", userProfile.getDisplayName())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(jwtConfig.getIssuer())
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public String getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Gets the authentication from a JWT token.
     *
     * @param token the JWT token
     * @return the authentication
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = claims.getSubject();
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        List<String> roles = claims.get("roles", ArrayList.class);

        // Create authorities from roles
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // Create user details
        JwtUserDetails userDetails = new JwtUserDetails(userId, email, name, authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }
}
