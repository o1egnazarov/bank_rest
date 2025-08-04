package ru.noleg.bankcards.controller.card;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.noleg.bankcards.mapper.CardMapper;
import ru.noleg.bankcards.service.CardService;

@TestConfiguration
public class TestCardControllerMocksConfig {

    @Bean
    public CardService cardService() {
        return Mockito.mock(CardService.class);
    }

    @Bean
    public CardMapper cardMapper() {
        return Mockito.mock(CardMapper.class);
    }
}