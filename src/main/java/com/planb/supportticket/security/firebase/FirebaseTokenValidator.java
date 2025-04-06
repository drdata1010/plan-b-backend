package com.planb.supportticket.security.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validator for Firebase JWT tokens.
 * Verifies and extracts information from Firebase tokens.
 */
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "firebase.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class FirebaseTokenValidator {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseTokenValidator.class);

    @Autowired(required = false)
    private FirebaseAuth firebaseAuth;

    @org.springframework.beans.factory.annotation.Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    // Simple token cache to improve performance
    private final Map<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    // Cache expiration time in milliseconds (5 minutes)
    private static final long CACHE_EXPIRATION = 5 * 60 * 1000;

    /**
     * Validates a Firebase JWT token and returns the user details.
     *
     * @param token the Firebase JWT token
     * @return the FirebaseUserDetails
     * @throws BadCredentialsException if the token is invalid
     */
    public FirebaseUserDetails validateToken(String token) {
        // If Firebase is disabled, create a mock user
        if (!firebaseEnabled || firebaseAuth == null) {
            logger.warn("Firebase is disabled, creating mock user for token: {}", token);
            return createMockUser(token);
        }

        try {
            // Check cache first
            CachedToken cachedToken = tokenCache.get(token);
            if (cachedToken != null && !cachedToken.isExpired()) {
                return cachedToken.getUserDetails();
            }

            // Verify the token with Firebase
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);

            // Get user details from Firebase
            Map<String, Object> claims = decodedToken.getClaims();

            // Create user details
            FirebaseUserDetails userDetails = new FirebaseUserDetails(claims);

            // Cache the token
            tokenCache.put(token, new CachedToken(userDetails));

            return userDetails;
        } catch (FirebaseAuthException e) {
            logger.error("Firebase Authentication failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid Firebase token", e);
        }
    }

    /**
     * Creates a mock user for development/testing when Firebase is disabled.
     *
     * @param token the token (used as user ID)
     * @return a mock FirebaseUserDetails
     */
    private FirebaseUserDetails createMockUser(String token) {
        // Create a simple mock user with basic claims
        Map<String, Object> claims = new java.util.HashMap<>();
        String userId = "mock-user-" + Math.abs(token.hashCode());

        claims.put("uid", userId);
        claims.put("email", userId + "@example.com");
        claims.put("email_verified", true);
        claims.put("name", "Mock User");
        claims.put("picture", "");

        // Add roles claim
        java.util.List<String> roles = java.util.Arrays.asList("ROLE_USER");
        claims.put("roles", roles);

        return new FirebaseUserDetails(claims);
    }

    /**
     * Clears the token cache.
     */
    public void clearCache() {
        tokenCache.clear();
    }

    /**
     * Removes expired tokens from the cache.
     */
    public void cleanupCache() {
        tokenCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Inner class for caching tokens with expiration.
     */
    private static class CachedToken {
        private final FirebaseUserDetails userDetails;
        private final long expirationTime;

        public CachedToken(FirebaseUserDetails userDetails) {
            this.userDetails = userDetails;
            this.expirationTime = System.currentTimeMillis() + CACHE_EXPIRATION;
        }

        public FirebaseUserDetails getUserDetails() {
            return userDetails;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}
