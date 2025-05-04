package com.planb.supportticket.security.config;

import com.planb.supportticket.security.firebase.FirebaseAuthenticationFilter;
import com.planb.supportticket.security.firebase.FirebaseTokenValidator;
import com.planb.supportticket.security.jwt.JwtAuthenticationFilter;
import com.planb.supportticket.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Main security configuration for the application.
 * Configures web security, Firebase authentication, role-based authorization,
 * WebSocket security, and CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private FirebaseTokenValidator firebaseTokenValidator;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${firebase.enabled:true}")
    private boolean firebaseEnabled;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    /**
     * Configures the main HTTP security filter chain.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Configure session management
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Configure authorization rules
        http.authorizeHttpRequests(authorize -> authorize
            // Public endpoints
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/auth/jwt/**").permitAll()
            .requestMatchers("/public/**").permitAll()
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

            // OPTIONS requests (for CORS preflight)
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // Role-based access for user types
            .requestMatchers("/api/user/**").hasRole("USER")
            .requestMatchers("/api/expert/**").hasRole("EXPERT")
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/support/**").hasAnyRole("SUPPORT", "ADMIN")

            // Role-based access for features
            .requestMatchers("/api/tickets/pick/**").hasRole("EXPERT")
            .requestMatchers("/api/tickets/assign/**").hasAnyRole("EXPERT", "ADMIN", "SUPPORT")
            .requestMatchers("/api/tickets/close/**").hasAnyRole("EXPERT", "ADMIN", "SUPPORT")
            .requestMatchers("/api/ai/**").hasAnyRole("USER", "EXPERT", "ADMIN")

            // For testing purposes, allow all requests
            .anyRequest().permitAll()
        );

        // Add JWT authentication filter
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        // Add Firebase authentication filter if enabled
        if (firebaseEnabled) {
            http.addFilterBefore(
                    new FirebaseAuthenticationFilter(firebaseTokenValidator),
                    JwtAuthenticationFilter.class
            );
        }

        return http.build();
    }



    /**
     * Configures CORS for the application.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Set allowed origins
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

        // Set allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Set allowed headers
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        configuration.setAllowedHeaders(headers);

        // Set exposed headers
        List<String> exposed = Arrays.asList(exposedHeaders.split(","));
        configuration.setExposedHeaders(exposed);

        // Set allow credentials
        configuration.setAllowCredentials(allowCredentials);

        // Set max age
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
