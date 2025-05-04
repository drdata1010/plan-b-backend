package com.planb.supportticket.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Configuration for authentication.
 */
@Configuration
public class AuthConfig {

    /**
     * Creates a password encoder bean.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates a user details service bean.
     *
     * @return the user details service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Create test users for each role
        UserDetails adminUser = User.builder()
                .username("admin@planbnext.com")
                .password(passwordEncoder().encode("Admin@123"))
                .roles("ADMIN")
                .build();

        UserDetails expertUser = User.builder()
                .username("expert@planbnext.com")
                .password(passwordEncoder().encode("Expert@123"))
                .roles("EXPERT")
                .build();

        UserDetails regularUser = User.builder()
                .username("user@planbnext.com")
                .password(passwordEncoder().encode("User@123"))
                .roles("USER")
                .build();

        UserDetails supportUser = User.builder()
                .username("support@planbnext.com")
                .password(passwordEncoder().encode("Support@123"))
                .roles("SUPPORT")
                .build();

        return new InMemoryUserDetailsManager(adminUser, expertUser, regularUser, supportUser);
    }

    /**
     * Creates an authentication manager bean.
     *
     * @param authenticationConfiguration the authentication configuration
     * @return the authentication manager
     * @throws Exception if an error occurs
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
