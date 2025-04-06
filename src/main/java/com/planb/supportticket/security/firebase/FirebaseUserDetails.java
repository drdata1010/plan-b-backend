package com.planb.supportticket.security.firebase;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of Spring Security's UserDetails for Firebase users.
 * Adapts Firebase user data to Spring Security's user representation.
 */
public class FirebaseUserDetails implements UserDetails {

    private final String uid;
    private final String email;
    private final String displayName;
    private final boolean emailVerified;
    private final String pictureUrl;
    private final List<GrantedAuthority> authorities;
    private final boolean enabled;

    /**
     * Constructs a FirebaseUserDetails from Firebase token claims.
     *
     * @param claims the Firebase token claims
     */
    @SuppressWarnings("unchecked")
    public FirebaseUserDetails(Map<String, Object> claims) {
        this.uid = (String) claims.get("user_id");
        this.email = (String) claims.get("email");
        this.displayName = (String) claims.get("name");
        this.emailVerified = claims.containsKey("email_verified") ? (boolean) claims.get("email_verified") : false;
        this.pictureUrl = (String) claims.get("picture");
        this.enabled = !Boolean.TRUE.equals(claims.get("disabled"));
        
        // Extract custom claims for roles
        Map<String, Object> customClaims = claims.containsKey("claims") 
                ? (Map<String, Object>) claims.get("claims") 
                : Map.of();
        
        // Convert roles from custom claims to Spring Security authorities
        List<String> roles = customClaims.containsKey("roles") 
                ? (List<String>) customClaims.get("roles") 
                : List.of("ROLE_USER"); // Default role
        
        this.authorities = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Gets the Firebase UID.
     *
     * @return the Firebase UID
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the user's display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the user's email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Checks if the user's email is verified.
     *
     * @return true if the email is verified, false otherwise
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Gets the user's profile picture URL.
     *
     * @return the profile picture URL
     */
    public String getPictureUrl() {
        return pictureUrl;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Firebase handles passwords, so we don't store them
        return null;
    }

    @Override
    public String getUsername() {
        // Use email as the username
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
