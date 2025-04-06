package com.planb.supportticket.security.firebase;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom authentication token for Firebase authentication.
 * Extends Spring Security's AbstractAuthenticationToken to integrate Firebase auth.
 */
public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String token;

    /**
     * Creates an unauthenticated token with the given token string.
     *
     * @param token the Firebase JWT token
     */
    public FirebaseAuthenticationToken(String token) {
        super(null);
        this.principal = null;
        this.token = token;
        setAuthenticated(false);
    }

    /**
     * Creates an authenticated token with the given principal and authorities.
     *
     * @param principal the authenticated user principal (FirebaseUserDetails)
     * @param token the Firebase JWT token
     * @param authorities the granted authorities
     */
    public FirebaseAuthenticationToken(Object principal, String token, 
                                      Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * Gets the Firebase JWT token.
     *
     * @return the Firebase JWT token
     */
    public String getToken() {
        return token;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }
}
