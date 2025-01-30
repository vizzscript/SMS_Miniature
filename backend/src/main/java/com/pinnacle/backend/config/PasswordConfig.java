package com.pinnacle.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        int saltLength = 16; // Default: 16
        int hashLength = 32; // Default: 32
        int parallelism = 1; // Default: 1
        int memoryCost = 4096; // Default: 4096 KB
        int iterations = 3; // Default: 3

        return new Argon2PasswordEncoder(
            saltLength,
            hashLength,
            parallelism,
            memoryCost,
            iterations
        );
        
    }
}
