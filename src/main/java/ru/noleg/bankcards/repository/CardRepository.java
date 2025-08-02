package ru.noleg.bankcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.noleg.bankcards.entity.Card;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    Optional<Card> findByIdAndOwnerId(Long cardId, Long ownerId);
}
