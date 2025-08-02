package ru.noleg.bankcards.service;

import org.springframework.data.domain.Sort;
import ru.noleg.bankcards.entity.Card;

import java.math.BigDecimal;
import java.util.List;

public interface CardService {

    Long create(Card card);

    Card get(Long id);

    List<Card> getAllByOwner(Long ownerId, Integer pageNumber, Integer pageSize, Sort sort);

    List<Card> getAll(Integer pageNumber, Integer pageSize, Sort sort);

    BigDecimal getBalance(Long cardId, Long ownerId);

    void activate(Long cardId, Long ownerId);

    void block(Long cardId, Long ownerId);

    void delete(Long id);

    void deposit(Long cardId, BigDecimal amount, Long ownerId);
}
