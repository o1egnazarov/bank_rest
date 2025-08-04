package ru.noleg.bankcards.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.noleg.bankcards.entity.Card;
import ru.noleg.bankcards.entity.CardStatus;
import ru.noleg.bankcards.exception.error.BusinessLogicException;
import ru.noleg.bankcards.exception.error.CardNotFoundException;
import ru.noleg.bankcards.exception.error.CardTransferException;
import ru.noleg.bankcards.repository.CardRepository;
import ru.noleg.bankcards.service.impl.CardTransferServiceImpl;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CardTransferServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardTransferServiceImpl cardTransferService;

    @Test
    void transfer_shouldTransferToCard_whenAllValid() {
        // Arrange
        Long ownerId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100L);

        Long fromCardId = 10L;
        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setBalance(BigDecimal.valueOf(200L));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpirationDate(YearMonth.now().plusMonths(1));

        Long toCardId = 20L;
        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setBalance(BigDecimal.valueOf(50L));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        // Act
        cardTransferService.transfer(ownerId, fromCardId, toCardId, amount);

        // Assert
        assertEquals(BigDecimal.valueOf(100L), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(150L), toCard.getBalance());

        verify(cardRepository, times(1))
                .findByIdAndOwnerId(fromCardId, ownerId);
        verify(cardRepository, times(1))
                .findByIdAndOwnerId(toCardId, ownerId);
        verify(cardRepository, times(1)).save(fromCard);
        verify(cardRepository, times(1)).save(toCard);
    }

    @Test
    void transfer_shouldThrowBusinessLogicException_whenAmountNegative() {
        // Arrange
        Long ownerId = 1L;
        Long fromCardId = 10L;
        Long toCardId = 10L;
        BigDecimal negativeAmount = BigDecimal.valueOf(-100L);

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                cardTransferService.transfer(ownerId, fromCardId, toCardId, negativeAmount));
        assertEquals("Amount can't be negative", ex.getMessage());

        verify(cardRepository, never()).findByIdAndOwnerId(ownerId, fromCardId);
        verify(cardRepository, never()).findByIdAndOwnerId(ownerId, toCardId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrowCardTransferException_whenSameCardIds() {
        // Arrange
        Long ownerId = 1L;
        Long fromCardId = 10L;
        Long toCardId = 10L;
        BigDecimal amount = BigDecimal.valueOf(100L);

        // Act | Assert
        CardTransferException ex = assertThrows(CardTransferException.class, () ->
                cardTransferService.transfer(ownerId, fromCardId, toCardId, amount));
        assertEquals("Can't transfer to the same card", ex.getMessage());

        verify(cardRepository, never()).findByIdAndOwnerId(ownerId, fromCardId);
        verify(cardRepository, never()).findByIdAndOwnerId(ownerId, toCardId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrowCardNotFoundException_whenFromCardNotFound() {
        // Arrange
        Long ownerId = 1L;
        Long fromCardId = 10L;
        Long toCardId = 20L;
        BigDecimal amount = BigDecimal.valueOf(100L);

        when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.empty());

        // Act | Assert
        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardTransferService.transfer(ownerId, fromCardId, toCardId, amount));
        assertEquals("Sender card not found with ID " + fromCardId, ex.getMessage());

        verify(cardRepository, times(1))
                .findByIdAndOwnerId(fromCardId, ownerId);
        verify(cardRepository, never())
                .findByIdAndOwnerId(toCardId, ownerId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrowCardNotFoundException_whenToCardNotFound() {
        // Arrange
        Long ownerId = 1L;
        Long fromCardId = 10L;
        Long toCardId = 20L;
        BigDecimal amount = BigDecimal.valueOf(100L);

        when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(new Card()));
        when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.empty());

        // Act | Assert
        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardTransferService.transfer(ownerId, fromCardId, toCardId, amount));
        assertEquals("Recipient card not found with ID " + toCardId, ex.getMessage());

        verify(cardRepository, times(1))
                .findByIdAndOwnerId(fromCardId, ownerId);
        verify(cardRepository, times(1))
                .findByIdAndOwnerId(toCardId, ownerId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrowCardTransferException_whenCardBlocked() {
        // Arrange
        Long ownerId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100L);

        Long fromCardId = 10L;
        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setStatus(CardStatus.BLOCKED);

        Long toCardId = 20L;
        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        // Act | Assert
        CardTransferException ex = assertThrows(CardTransferException.class, () ->
                cardTransferService.transfer(ownerId, fromCardId, toCardId, amount));
        assertTrue(ex.getMessage().contains("non active"));

        verify(cardRepository, times(1))
                .findByIdAndOwnerId(fromCardId, ownerId);
        verify(cardRepository, times(1))
                .findByIdAndOwnerId(toCardId, ownerId);
        verify(cardRepository, never()).save(fromCard);
        verify(cardRepository, never()).save(toCard);
    }

    @Test
    void transfer_shouldThrowCardTransferException_whenCardExpired() {
        // Arrange
        Long ownerId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100L);

        Long fromCardId = 10L;
        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpirationDate(YearMonth.now().minusMonths(1));

        Long toCardId = 20L;
        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        // Act | Assert
        CardTransferException ex = assertThrows(CardTransferException.class, () ->
                cardTransferService.transfer(ownerId, fromCardId, toCardId, amount));
        assertTrue(ex.getMessage().contains("has expired"));

        verify(cardRepository, times(1))
                .findByIdAndOwnerId(fromCardId, ownerId);
        verify(cardRepository, times(1))
                .findByIdAndOwnerId(toCardId, ownerId);
        verify(cardRepository, never()).save(fromCard);
        verify(cardRepository, never()).save(toCard);
    }

    @Test
    void transfer_shouldTransferToCard_whenInsufficientFunds() {
        // Arrange
        Long ownerId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100L);

        Long fromCardId = 10L;
        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setBalance(BigDecimal.valueOf(50L));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpirationDate(YearMonth.now().plusMonths(1));

        Long toCardId = 20L;
        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setBalance(BigDecimal.valueOf(50L));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(fromCardId, ownerId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(toCardId, ownerId)).thenReturn(Optional.of(toCard));

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () ->
                cardTransferService.transfer(ownerId, fromCardId, toCardId, amount));
        assertEquals("Insufficient funds on the sender card.", ex.getMessage());

        verify(cardRepository, times(1))
                .findByIdAndOwnerId(fromCardId, ownerId);
        verify(cardRepository, times(1))
                .findByIdAndOwnerId(toCardId, ownerId);
        verify(cardRepository, never()).save(fromCard);
        verify(cardRepository, never()).save(toCard);
    }
}