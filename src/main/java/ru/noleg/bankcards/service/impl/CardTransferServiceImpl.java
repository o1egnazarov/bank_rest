package ru.noleg.bankcards.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.noleg.bankcards.entity.Card;
import ru.noleg.bankcards.entity.CardStatus;
import ru.noleg.bankcards.exception.error.BusinessLogicException;
import ru.noleg.bankcards.exception.error.CardNotFoundException;
import ru.noleg.bankcards.exception.error.CardTransferException;
import ru.noleg.bankcards.repository.CardRepository;
import ru.noleg.bankcards.service.CardTransferService;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@Transactional
public class CardTransferServiceImpl implements CardTransferService {

    private static final Logger logger = LoggerFactory.getLogger(CardTransferServiceImpl.class);

    private final CardRepository cardRepository;

    public CardTransferServiceImpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public void transfer(Long ownerId, Long fromCardId, Long toCardId, BigDecimal amount) {
        logger.debug("Initiating transfer: ownerId={}, fromCardId={}, toCardId={}, amount={}",
                ownerId, fromCardId, toCardId, amount
        );

        this.validateTransferParameters(fromCardId, toCardId, amount);

        Card fromCard = cardRepository.findByIdAndOwnerId(fromCardId, ownerId).orElseThrow(() -> {
            logger.error("Sender card not found: id={}, ownerId={}", fromCardId, ownerId);
            return new CardNotFoundException("Sender card not found with ID " + fromCardId);
        });

        Card toCard = cardRepository.findByIdAndOwnerId(toCardId, ownerId).orElseThrow(() -> {
            logger.error("Recipient card not found: id={}, ownerId={}", toCardId, ownerId);
            return new CardNotFoundException("Recipient card not found with ID " + toCardId);
        });

        this.validateCardActive(fromCard);
        this.validateCardActive(toCard);

        if (fromCard.getBalance().compareTo(amount) < 0) {
            logger.error("Insufficient funds: cardId={}, attemptedTransfer={}", fromCardId, amount);
            throw new BusinessLogicException("Insufficient funds on the sender card.");
        }

        this.performTransfer(amount, fromCard, toCard);

        logger.debug("Transfer completed: {} -> {}, amount={}", fromCardId, toCardId, amount);
    }

    private void validateTransferParameters(Long fromCardId, Long toCardId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Transfer failed: negative or null amount: {}", amount);
            throw new BusinessLogicException("Amount can't be negative");
        }

        if (fromCardId.equals(toCardId)) {
            logger.error("Transfer failed: sender and recipient cards are the same: {}", fromCardId);
            throw new CardTransferException("Can't transfer to the same card");
        }
    }

    private void validateCardActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            logger.error("Card is not active: id={}, status={}", card.getId(), card.getStatus());
            throw new CardTransferException("Card with id: " + card.getId() + " non active");
        }
        if (card.getExpirationDate().isBefore(YearMonth.now())) {
            logger.error("Card is expired: id={}, expirationDate={}", card.getId(), card.getExpirationDate());
            throw new CardTransferException("Card with id: " + card.getId() + " has expired");
        }
    }

    private void performTransfer(BigDecimal amount, Card fromCard, Card toCard) {
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
