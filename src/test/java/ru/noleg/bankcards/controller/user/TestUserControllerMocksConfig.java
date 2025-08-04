package ru.noleg.bankcards.controller.user;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.noleg.bankcards.mapper.UserMapper;
import ru.noleg.bankcards.service.UserService;

@TestConfiguration
public class TestUserControllerMocksConfig {

    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public UserMapper userMapper() {
        return Mockito.mock(UserMapper.class);
    }
}