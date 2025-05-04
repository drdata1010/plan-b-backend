package com.planb.supportticket.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to authenticate requests with JWT tokens.
 * Extracts and validates JWT tokens from HTTP requests.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtTokenProvider tokenProvider;
    
    /**
     * Constructs a JwtAuthenticationFilter with the given token provider.
     *
     * @param tokenProvider the JWT token provider
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Extract token from request
            String token = extractToken(request);
            
            if (token != null && tokenProvider.validateToken(token)) {
                // Get authentication from token
                Authentication auth = tokenProvider.getAuthentication(token);
                
                // Set authentication in context
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                logger.debug("Authenticated user: {}", auth.getName());
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
            // Don't set authentication - let the request continue unauthenticated
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extracts the JWT token from the request.
     *
     * @param request the HTTP request
     * @return the JWT token, or null if not found
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
