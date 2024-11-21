package dev.formation.JavaCompiler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactiver CSRF pour les tests
                .cors(cors -> cors.disable()) // Désactiver CORS pour les tests
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Permettre toutes les requêtes

        return http.build();
    }
}

