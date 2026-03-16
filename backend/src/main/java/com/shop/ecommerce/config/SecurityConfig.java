package com.shop.ecommerce.config;

import com.shop.ecommerce.repository.UserRepository;
import com.shop.ecommerce.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Spring Security configuration.
 * Defines which endpoints are public, which require authentication,
 * and how authentication is performed.
 */
@Configuration
@EnableWebSecurity // Enables Spring Security for the whole application
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    // @Lazy breaks the circular dependency:
    // JwtAuthenticationFilter needs SecurityConfig, SecurityConfig needs JwtAuthenticationFilter
    // @Lazy tells Spring: don't create JwtAuthenticationFilter immediately, wait until it's needed
    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter,
                          UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
    }
    /**
     * Defines the security filter chain — the core of Spring Security configuration.
     * Every HTTP request passes through these rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for REST APIs with JWT
            .csrf(AbstractHttpConfigurer::disable)

            // Define which endpoints are public and which require authentication
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                // All other endpoints require a valid JWT token
                .anyRequest().authenticated()
            )

            // Use stateless sessions — no server-side session storage
            // Each request must carry its own JWT token
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our custom authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT filter before the default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Loads user details from the database by email.
     * Used by Spring Security during authentication.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username
                ));
    }

    /**
     * Authentication provider — connects UserDetailsService with PasswordEncoder.
     * Spring Security uses this to verify credentials during login.
     */
    @Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}

    /**
     * AuthenticationManager — used in AuthService to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder using BCrypt hashing algorithm.
     * Passwords are NEVER stored as plain text — always hashed.
     * BCrypt automatically handles salting.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 
