package ru.noleg.bankcards.controller;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.noleg.bankcards.security.jwt.JwtRequestFilter;
import ru.noleg.bankcards.security.jwt.TokenProvider;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class JwtTestSecurityConfig {

    @Bean
    public TokenProvider tokenProvider() {
        return Mockito.mock(TokenProvider.class);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return Mockito.mock(UserDetailsService.class);
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter(TokenProvider tokenProvider, UserDetailsService userDetailsService) {
        return new JwtRequestFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}