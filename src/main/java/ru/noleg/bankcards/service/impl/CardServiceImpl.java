package ru.noleg.bankcards.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.noleg.bankcards.entity.Card;
import ru.noleg.bankcards.entity.CardStatus;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.exception.error.BusinessLogicException;
import ru.noleg.bankcards.exception.error.CardNotFoundException;
import ru.noleg.bankcards.exception.error.UserNotFoundException;
import ru.noleg.bankcards.repository.CardRepository;
import ru.noleg.bankcards.repository.UserRepository;
import ru.noleg.bankcards.service.CardService;
import ru.noleg.bankcards.util.AesEncryptionUtil;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardServiceImpl implements CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);
    private static final int CARD_NUMBER_LENGTH = 16;

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AesEncryptionUtil aesEncryptionUtil;

    public CardServiceImpl(CardRepository cardRepository,
                           UserRepository userRepository,
                           AesEncryptionUtil aesEncryptionUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.aesEncryptionUtil = aesEncryptionUtil;
    }

    @Override
    public Long create(Card card) {
        Long ownerId = card.getOwner().getId();
        User owner = userRepository.findById(ownerId).orElseThrow(() -> {
            logger.error("User not found during card creation: userId={}", ownerId);
            return new UserNotFoundException("User not found by id " + ownerId);
        });

        String cardNumber = generateRandomCardNumber();
        card.setOwner(owner);
        card.setStatus(CardStatus.ACTIVE);
        card.setEncryptedNumber(aesEncryptionUtil.encrypt(cardNumber));

        Card savedCard = cardRepository.save(card);
        logger.debug("Card created successfully: cardId={}, ownerId={}", savedCard.getId(), ownerId);

        return savedCard.getId();
    }

    private String generateRandomCardNumber() {
        Random random = new SecureRandom();
        return random.ints(CARD_NUMBER_LENGTH, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    @Override
    @Transactional(readOnly = true)
    public Card get(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> {
            logger.error("Card not found: cardId={}", id);
            return new CardNotFoundException("Card not found by id " + id);
        });

        this.decryptCardNumber(card);
        return card;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getAllByOwner(Long ownerId, Integer pageNumber, Integer pageSize, Sort sort) {
        Page<Card> cards = cardRepository.findAllByOwnerId(ownerId, PageRequest.of(pageNumber, pageSize, sort));

        cards.forEach(this::decryptCardNumber);
        logger.debug("Retrieved {} cards for owner with id: {}", cards.getNumberOfElements(), ownerId);

        return cards.getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Card> getAll(Integer pageNumber, Integer pageSize, Sort sort) {
        Page<Card> cards = cardRepository.findAll(PageRequest.of(pageNumber, pageSize, sort));

        cards.forEach(this::decryptCardNumber);
        logger.debug("Retrieved {} cards", cards.getNumberOfElements());

        return cards.getContent();
    }

    private void decryptCardNumber(Card card) {
        try {
            String decryptedNumber = aesEncryptionUtil.decrypt(card.getEncryptedNumber());
            card.setMaskedNumber(decryptedNumber);
        } catch (Exception e) {
            logger.error("Failed to decrypt card number: cardId={}", card.getId(), e);
            throw new SecurityException("Could not decrypt card number", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long cardId, Long ownerId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, ownerId).orElseThrow(() -> {
            logger.error("Card not found for balance check: cardId={}, ownerId={}", cardId, ownerId);
            return new CardNotFoundException("Card with id " + cardId + " not found for owner " + ownerId);
        });

        this.validateCardActive(card);
        return card.getBalance();
    }

    @Override
    public void activate(Long cardId, Long ownerId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, ownerId).orElseThrow(() -> {
            logger.error("Card ownership validation failed for activation: cardId={}, ownerId={}", cardId, ownerId);
            return new CardNotFoundException("Card with id " + cardId + " not found for owner " + ownerId);
        });

        if (card.getExpirationDate().isBefore(YearMonth.now())) {
            logger.error("Attempt to activate expired card: cardId={}", cardId);
            throw new BusinessLogicException("Card with id: " + card.getId() + " has expired");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        logger.debug("Card activated: cardId={}, ownerId={}", cardId, ownerId);
    }

    @Override
    public void block(Long cardId, Long ownerId) {
        Card card = cardRepository.findByIdAndOwnerId(cardId, ownerId).orElseThrow(() -> {
            logger.error("Card ownership validation failed for blocking: cardId={}, ownerId={}", cardId, ownerId);
            return new CardNotFoundException("Card with id " + cardId + " not found for owner " + ownerId);
        });

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        logger.debug("Card blocked: cardId={}, ownerId={}", cardId, ownerId);
    }

    @Override
    public void delete(Long id) {
        if (cardRepository.existsById(id)) {
            cardRepository.deleteById(id);
            logger.debug("Card deleted: cardId={}", id);
        } else {
            logger.warn("Attempt to delete non-existent card: cardId={}", id);
        }
    }

    @Override
    public void deposit(Long cardId, BigDecimal amount, Long ownerId) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Deposit failed: negative or null amount: {}", amount);
            throw new BusinessLogicException("Amount must be greater than zero");
        }

        Card card = cardRepository.findByIdAndOwnerId(cardId, ownerId).orElseThrow(() -> {
            logger.error("Card ownership validation failed for deposit: cardId={}, ownerId={}", cardId, ownerId);
            return new CardNotFoundException("Card with id " + cardId + " not found for owner " + ownerId);
        });

        this.validateCardActive(card);

        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);

        logger.debug("Deposit successful: cardId={}, amount={}, ownerId={}", cardId, amount, ownerId);
    }

    private void validateCardActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            logger.error("Card is not active: id={}, status={}", card.getId(), card.getStatus());
            throw new BusinessLogicException("Card with id: " + card.getId() + " non active");
        }
        if (card.getExpirationDate().isBefore(YearMonth.now())) {
            logger.error("Card is expired: id={}, expirationDate={}", card.getId(), card.getExpirationDate());
            throw new BusinessLogicException("Card with id: " + card.getId() + " has expired");
        }
    }
}
