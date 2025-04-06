package com.planb.supportticket.security.firebase;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to authenticate requests with Firebase tokens.
 * Extracts and validates Firebase JWT tokens from HTTP requests.
 */
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
    
    private final FirebaseTokenValidator tokenValidator;
    
    /**
     * Constructs a FirebaseAuthenticationFilter with the given token validator.
     *
     * @param tokenValidator the Firebase token validator
     */
    public FirebaseAuthenticationFilter(FirebaseTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Extract token from request
            String token = extractToken(request);
            
            if (token != null) {
                // Validate token and get user details
                FirebaseUserDetails userDetails = tokenValidator.validateToken(token);
                
                // Create authenticated token
                FirebaseAuthenticationToken authToken = new FirebaseAuthenticationToken(
                        userDetails, token, userDetails.getAuthorities());
                
                // Set authentication in context
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                logger.debug("Authenticated user: {}", userDetails.getUsername());
            }
        } catch (BadCredentialsException e) {
            logger.debug("Invalid Firebase token: {}", e.getMessage());
            // Don't set authentication - let the request continue unauthenticated
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
            // Don't set authentication - let the request continue unauthenticated
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extracts the Firebase token from the request.
     *
     * @param request the HTTP request
     * @return the Firebase token, or null if not found
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
