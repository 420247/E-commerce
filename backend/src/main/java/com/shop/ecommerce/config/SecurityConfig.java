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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import java.util.List;

/**
 * Main Spring Security configuration.
 *
 * Spring Security works as a chain of filters — every HTTP request passes through
 * them in order before reaching the controller. This class defines:
 *   1. Which endpoints are public and which require a valid JWT token.
 *   2. How CORS is handled so Angular (port 4200) can call the backend (port 8080).
 *   3. How users are loaded from the database and their passwords verified.
 *   4. How JWT tokens are validated on each request.
 *
 * @Configuration — tells Spring this class contains bean definitions (methods annotated with @Bean).
 * @EnableWebSecurity — activates Spring Security's web support for the whole application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    /**
     * Constructor injection is preferred over @Autowired because it makes dependencies explicit
     * and allows the class to be instantiated without Spring (useful in unit tests).
     *
     * @Lazy on JwtAuthenticationFilter breaks a circular dependency:
     * JwtAuthenticationFilter depends on SecurityConfig (needs UserDetailsService),
     * and SecurityConfig depends on JwtAuthenticationFilter (adds it to the filter chain).
     * @Lazy tells Spring: "don't create JwtAuthenticationFilter immediately at startup —
     * create it lazily the first time it's actually needed". This breaks the cycle.
     */
    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter,
                          UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userRepository = userRepository;
    }

    /**
     * Defines CORS (Cross-Origin Resource Sharing) rules for the entire application.
     *
     * CORS is a browser security mechanism that blocks requests from a different origin
     * (protocol + domain + port). Since Angular runs on localhost:4200 and our backend
     * on localhost:8080, the browser treats them as different origins and blocks requests
     * unless the server explicitly allows it via CORS headers.
     *
     * This bean is registered directly inside Spring Security (via .cors(...)) rather than
     * as a separate filter, because Security must process CORS headers BEFORE applying
     * authentication rules. Without this, preflight OPTIONS requests (sent by the browser
     * before every cross-origin request) would be blocked with 403 Forbidden.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow requests from the Angular dev server
        config.setAllowedOrigins(List.of(
            "http://localhost",
            "http://localhost:4200",
            "https://e-commerce-dmxk.onrender.com"
            ));

        // Allow standard REST methods + OPTIONS (OPTIONS is used for preflight checks)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all request headers, including Authorization (required for JWT)
        config.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, Authorization header) to be included in requests
        config.setAllowCredentials(true);

        // Apply this CORS configuration to all endpoints in the application
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    

    /**
     * The security filter chain — the heart of the configuration.
     *
     * Think of this as a series of checkpoints that every HTTP request passes through.
     * The order of rules matters: Spring Security evaluates them top to bottom and
     * applies the first matching rule.
     *
     * HttpSecurity is a builder — each method call configures one aspect of security
     * and returns the same builder so calls can be chained.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            

            // Disable CSRF protection — not needed for stateless REST APIs.
            // CSRF attacks exploit session cookies; since we use JWT (not cookies), there's no risk.
            .csrf(csrf -> csrf.disable())
.cors(Customizer.withDefaults()) // Enable CORS with the configuration defined in corsConfigurationSource()
            // Define access rules for each endpoint
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — anyone can call these without a token
                .requestMatchers("/api/auth/**").permitAll()   // register, login
                .requestMatchers("/api/products/**").permitAll() // product listing
                .requestMatchers("/api/ai/**").permitAll()     // AI search
                // Everything else (wishlist, etc.) requires a valid JWT token
                .anyRequest().authenticated()
            )
            
            // Use stateless session management — Spring Security will NOT create or use
            // HTTP sessions. Every request must carry its own JWT token.
            // This is essential for horizontal scaling (multiple server instances).
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our custom authentication provider (see authenticationProvider() below)
            .authenticationProvider(authenticationProvider())
            

            // Insert our JWT filter BEFORE Spring's built-in UsernamePasswordAuthenticationFilter.
            // This ensures the JWT is validated and the user is set in the SecurityContext
            // before any authorization decisions are made.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            

        return http.build();
    }

    /**
     * Tells Spring Security how to load a user from the database given their username (email).
     * This is called by JwtAuthenticationFilter after extracting the email from the JWT token.
     *
     * The lambda here is an implementation of the UserDetailsService interface —
     * a functional interface with a single method: loadUserByUsername(String username).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username
                ));
    }

    /**
     * Connects UserDetailsService and PasswordEncoder into a single authentication provider.
     *
     * DaoAuthenticationProvider is Spring Security's standard implementation that:
     *   1. Loads the user from the database via UserDetailsService.
     *   2. Checks that the provided password matches the stored BCrypt hash.
     *   3. Returns an authenticated token if both checks pass.
     *
     * This provider is used by AuthenticationManager in AuthService during login.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a Spring bean so it can be injected into AuthService.
     * AuthService calls authenticationManager.authenticate(...) during login to verify credentials.
     * Spring creates the AuthenticationManager internally — we just expose it here.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder using the BCrypt hashing algorithm.
     *
     * BCrypt is a one-way hashing function designed specifically for passwords.
     * It automatically generates a random salt and embeds it in the hash,
     * so two identical passwords produce different hashes — this prevents rainbow table attacks.
     * Passwords are NEVER stored as plain text — only their BCrypt hash is saved in the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
