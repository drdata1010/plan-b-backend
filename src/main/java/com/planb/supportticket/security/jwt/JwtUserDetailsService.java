package com.planb.supportticket.security.jwt;

import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.UserRole;
import com.planb.supportticket.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserDetailsService for JWT authentication.
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserProfile userProfile = userService.getUserProfileByEmail(email);
            
            // Convert roles to Spring Security authorities
            List<GrantedAuthority> authorities = userProfile.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(UserProfile.getSpringSecurityRoleName(role)))
                    .collect(Collectors.toList());
            
            return new JwtUserDetails(
                    userProfile.getId().toString(),
                    userProfile.getEmail(),
                    userProfile.getDisplayName(),
                    authorities
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with email: " + email, e);
        }
    }
}
