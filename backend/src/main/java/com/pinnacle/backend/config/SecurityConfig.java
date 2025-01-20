package com.pinnacle.backend.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer; // Import Customizer
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Enable CORS with default configuration
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow all OPTIONS requests
                .requestMatchers(HttpMethod.POST, "/client/signup").permitAll() // Allow access to /client/signup
                .requestMatchers(HttpMethod.POST, "/client/login").permitAll() // Allow access to /client/login
                .requestMatchers(HttpMethod.GET, "/client/hello").permitAll() // Allow access to /client/hello
                .anyRequest().authenticated() // Secure all other endpoints
            )
            .httpBasic(Customizer.withDefaults()) // Use HTTP Basic authentication with default settings
            .csrf(csrf -> csrf.disable()); // Disable CSRF for simplicity (enable in production)
        return http.build();
    }
}