package com.lumina.backend.infrastructure.config;

import com.lumina.backend.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // --- 1. SWAGGER & OPENAPI ---
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // --- 2. PUBLIC ENDPOINTS ---
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/files/**").permitAll()

                        // QR Verification
                        .requestMatchers("/api/medical-records/prescriptions/*/verify").permitAll()
                        .requestMatchers("/api/medical-records/prescriptions/*/pdf").permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- 3. ROLE BASED ACCESS ---
                        // Public GET requests for basic info
                        .requestMatchers(HttpMethod.GET, "/doctors/**", "/branches/**").permitAll()

                        // Admin Only
                        .requestMatchers(HttpMethod.POST, "/doctors/**", "/branches/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/doctors/**", "/branches/**").hasRole("ADMIN")

                        // Doctor Update
                        .requestMatchers(HttpMethod.PUT, "/doctors/**").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.PATCH, "/doctors/**").hasAnyRole("ADMIN", "DOCTOR")

                        // Registrar
                        .requestMatchers(HttpMethod.POST, "/auth/register-patient-on-desk").hasAnyRole("REGISTRAR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/appointments/book-for-patient").hasAnyRole("REGISTRAR")
                        .requestMatchers(HttpMethod.POST, "/invoices/**").hasRole("REGISTRAR")

                        // Lab Technician
                        .requestMatchers(HttpMethod.POST, "/api/medical-records/results").hasRole("LAB_TECHNICIAN")

                        // Authenticated Users
                        .requestMatchers(HttpMethod.GET, "/api/medical-records/**").authenticated()
                        .requestMatchers("/appointments/**").authenticated()
                        .requestMatchers("/users/**").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "https://lumina-medical.web.app",
                "https://lumina-backend-*.onrender.com",
                "http://localhost:*"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Origin", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        );
    }
}