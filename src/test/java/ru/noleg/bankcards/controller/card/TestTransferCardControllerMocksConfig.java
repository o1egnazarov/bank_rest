package ru.noleg.bankcards.controller.card;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.noleg.bankcards.service.CardTransferService;

@TestConfiguration
public class TestTransferCardControllerMocksConfig {

    @Bean
    public CardTransferService cardTransferService() {
        return Mockito.mock(CardTransferService.class);
    }

}