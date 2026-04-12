package com.shop.ecommerce.config;

import com.shop.ecommerce.repository.UserRepository;
import com.shop.ecommerce.security.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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

/**
 * Spring Security configuration for the e-commerce application.
 *
 * <p>Defines which endpoints are public, how JWT tokens are validated, how users are loaded from
 * the database, and how CORS is handled between the Angular frontend (port 4200) and this backend
 * (port 8080).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final UserRepository userRepository;

  /**
   * @param jwtAuthFilter the filter that validates JWT tokens on each request. Marked {@code @Lazy}
   *     to break a circular dependency: {@code JwtAuthenticationFilter} needs {@code
   *     UserDetailsService}, which is defined in this class.
   * @param userRepository used to look up users by email during authentication.
   */
  public SecurityConfig(
      @Lazy JwtAuthenticationFilter jwtAuthFilter, UserRepository userRepository) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.userRepository = userRepository;
  }

  /**
   * Configures CORS so the Angular dev server can call this backend.
   *
   * <p>Registered inside Spring Security (not as a standalone filter) so that preflight {@code
   * OPTIONS} requests are allowed before authentication kicks in. Without this, the browser blocks
   * every cross-origin request with a CORS error.
   *
   * @return CORS rules applied to all endpoints ({@code /**})
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(
        List.of(
            "http://localhost", "http://localhost:4200", "https://e-commerce-dmxk.onrender.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * Defines the security filter chain — the list of rules every HTTP request is checked against.
   *
   * <p>Rules are evaluated top to bottom; the first match applies. Key decisions made here:
   *
   * <ul>
   *   <li>CSRF is disabled — we use stateless JWT, not session cookies, so there is no risk.
   *   <li>Auth, product, and AI endpoints are public; everything else requires a valid token.
   *   <li>Sessions are stateless — no server-side session is created or stored.
   *   <li>{@link JwtAuthenticationFilter} runs before Spring's default login filter.
   * </ul>
   *
   * @param http the builder used to configure HTTP security
   * @return the built {@link SecurityFilterChain}
   * @throws Exception if the configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/products/**")
                    .permitAll()
                    .requestMatchers("/api/ai/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Loads a user from the database by email.
   *
   * <p>Called by {@link JwtAuthenticationFilter} after extracting the email claim from the token.
   *
   * @return a {@link UserDetailsService} that queries the database by email
   * @throws UsernameNotFoundException if no user with the given email exists
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return username ->
        userRepository
            .findByEmail(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + username));
  }

  /**
   * Wires {@link UserDetailsService} and {@link PasswordEncoder} into a single provider.
   *
   * <p>Used by Spring Security during login to load the user and verify the password hash.
   *
   * @return a configured {@link DaoAuthenticationProvider}
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Exposes the {@link AuthenticationManager} as a bean so {@code AuthService} can inject it.
   *
   * <p>{@code AuthService} calls {@code authenticationManager.authenticate(...)} during login to
   * delegate credential verification to the configured {@link AuthenticationProvider}.
   *
   * @param config Spring's internal authentication configuration
   * @return the application-wide {@link AuthenticationManager}
   * @throws Exception if the manager cannot be retrieved
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Password encoder using BCrypt.
   *
   * <p>BCrypt embeds a random salt in every hash, so identical passwords produce different hashes.
   * This makes rainbow table attacks ineffective. Plain-text passwords are never stored in the
   * database.
   *
   * @return a {@link BCryptPasswordEncoder} instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
