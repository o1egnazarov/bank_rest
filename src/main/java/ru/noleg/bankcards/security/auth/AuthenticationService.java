package ru.noleg.bankcards.security.auth;

import ru.noleg.bankcards.entity.User;

public interface AuthenticationService {

    Long signUp(User user);

    String signIn(String username, String password);
}