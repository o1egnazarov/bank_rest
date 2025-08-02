package ru.noleg.bankcards.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface TokenProvider {

    String generateToken(UserDetails userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    String extractUsername(String token);
}
