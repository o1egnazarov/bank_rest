package ru.noleg.bankcards.controller.auth;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.noleg.bankcards.mapper.UserMapper;
import ru.noleg.bankcards.security.auth.AuthenticationService;

@TestConfiguration
public class TestAuthControllerMocksConfig {

    @Bean
    public AuthenticationService authenticationService() {
        return Mockito.mock(AuthenticationService.class);
    }

    @Bean
    public UserMapper userMapper() {
        return Mockito.mock(UserMapper.class);
    }
}