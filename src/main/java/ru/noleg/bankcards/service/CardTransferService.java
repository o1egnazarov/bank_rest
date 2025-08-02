package ru.noleg.bankcards.service;

import java.math.BigDecimal;

public interface CardTransferService {
    void transfer(Long ownerId, Long fromCardId, Long toCardId, BigDecimal amount);
}
