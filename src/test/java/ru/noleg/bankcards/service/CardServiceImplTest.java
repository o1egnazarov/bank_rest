package ru.noleg.bankcards.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.noleg.bankcards.entity.Card;
import ru.noleg.bankcards.entity.CardStatus;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.exception.error.BusinessLogicException;
import ru.noleg.bankcards.exception.error.CardNotFoundException;
import ru.noleg.bankcards.exception.error.UserNotFoundException;
import ru.noleg.bankcards.repository.CardRepository;
import ru.noleg.bankcards.repository.UserRepository;
import ru.noleg.bankcards.service.impl.CardServiceImpl;
import ru.noleg.bankcards.util.AesEncryptionUtil;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AesEncryptionUtil aesEncryptionUtil;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void create_shouldSaveEncryptedCard_whenValid() {
        // Arrange
        Long ownerId = 1L;
        User owner = new User();
        owner.setId(ownerId);

        Card card = new Card();
        card.setOwner(owner);

        Long savedCardId = 100L;
        Card savedCard = new Card();
        savedCard.setId(savedCardId);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(aesEncryptionUtil.encrypt(any())).thenReturn("encryptedNumber");
        when(cardRepository.save(any())).thenReturn(savedCard);

        // Act
        Long result = cardService.create(card);

        // Assert
        assertEquals(100L, result);
        verify(userRepository, times(1)).findById(1L);
        verify(aesEncryptionUtil, times(1)).encrypt(any());
        verify(cardRepository, times(1)).save(any());
    }

    @Test
    void create_shouldThrowUserNotFoundException_whenUserNotExists() {
        // Arrange
        Card card = new Card();

        User user = new User();
        Long ownerId = 1L;
        user.setId(1L);
        card.setOwner(user);

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        // Act | Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> cardService.create(card));
        assertEquals("User not found by id 1", ex.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(aesEncryptionUtil, never()).encrypt(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void get_shouldReturnDecryptedCard_whenExists() {
        // Arrange
        String encryptedNumber = "enc";
        String decryptedNumber = "1234567812345678";

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setEncryptedNumber(encryptedNumber);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(aesEncryptionUtil.decrypt(encryptedNumber)).thenReturn(decryptedNumber);

        // Act
        Card result = cardService.get(cardId);

        // Assert
        assertEquals(decryptedNumber, result.getMaskedNumber());
        verify(cardRepository, times(1)).findById(cardId);
        verify(aesEncryptionUtil, times(1)).decrypt(encryptedNumber);
    }

    @Test
    void get_shouldThrowCardNotFoundException_whenCardNotExists() {
        // Arrange
        Long cardId = 1L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act | Assert
        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardService.get(cardId));
        assertEquals("Card not found by id 1", ex.getMessage());

        verify(cardRepository, times(1)).findById(cardId);
    }

    @Test
    void findAllByOwner_shouldReturnCards_whenUserExists() {
        // Arrange
        Long ownerId = 1L;

        int pageNumber = 1;
        int pageSize = 10;
        Sort sort = mock(Sort.class);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        List<Card> cardList = List.of(mock(Card.class), mock(Card.class));
        Page<Card> cards = new PageImpl<>(cardList, pageRequest, cardList.size());

        when(cardRepository.findAllByOwnerId(ownerId, pageRequest)).thenReturn(cards);

        // Act
        List<Card> result = cardService.getAllByOwner(ownerId, pageNumber, pageSize, sort);

        // Assert
        assertEquals(2, result.size());
        verify(cardRepository, times(1)).findAllByOwnerId(ownerId, pageRequest);
    }

    @Test
    void findAll_shouldReturnCards_whenUserExists() {
        // Arrange
        int pageNumber = 1;
        int pageSize = 10;
        Sort sort = mock(Sort.class);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        List<Card> cardList = List.of(mock(Card.class), mock(Card.class));
        Page<Card> cards = new PageImpl<>(cardList, pageRequest, cardList.size());

        when(cardRepository.findAll(pageRequest)).thenReturn(cards);

        // Act
        List<Card> result = cardService.getAll(pageNumber, pageSize, sort);

        // Assert
        assertEquals(2, result.size());
        verify(cardRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getBalance_shouldReturnBalance_whenCardExists() {
        // Arrange
        Long ownerId = 1L;
        BigDecimal balance = BigDecimal.valueOf(1000.0);

        Long cardId = 1L;
        Card card = new Card();
        card.setBalance(balance);
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act
        BigDecimal result = cardService.getBalance(cardId, ownerId);

        // Assert
        assertEquals(balance, result);

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
    }

    @Test
    void getBalance_shouldThrowCardNotFoundException_whenCardNotExists() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.empty());

        // Act | Assert
        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardService.getBalance(cardId, ownerId)
        );
        assertEquals("Card with id " + cardId + " not found for owner " + ownerId, ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
    }

    @Test
    void getBalance_shouldThrowBusinessLogicException_whenCardBlocked() {
        // Arrange
        Long ownerId = 1L;

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);
        card.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> cardService.getBalance(cardId, ownerId)
        );
        assertEquals("Card with id: " + cardId + " non active", ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
    }

    @Test
    void getBalance_shouldThrowBusinessLogicException_whenCardExpired() {
        // Arrange
        Long ownerId = 1L;

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(YearMonth.now().minusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> cardService.getBalance(cardId, ownerId)
        );
        assertEquals("Card with id: " + cardId + " has expired", ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
    }

    @Test
    void activate_shouldUpdateCardStatus_whenCardExists() {
        // Arrange
        Long ownerId = 1L;

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);
        card.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act
        cardService.activate(cardId, ownerId);

        // Assert
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void activate_shouldThrowCardNotFoundException_whenCardNotExists() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.empty());

        // Act | Assert
        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardService.activate(cardId, ownerId)
        );
        assertEquals("Card with id " + cardId + " not found for owner " + ownerId, ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void activate_shouldThrowBusinessLogicException_whenCardExpired() {
        // Arrange
        Long ownerId = 1L;

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);
        card.setExpirationDate(YearMonth.now().minusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> cardService.activate(cardId, ownerId)
        );
        assertEquals("Card with id: " + cardId + " has expired", ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(card);
    }

    @Test
    void block_shouldUpdateCardStatus_whenCardExists() {
        // Arrange
        Long ownerId = 1L;

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act
        cardService.block(cardId, ownerId);

        // Assert
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void block_shouldThrowCardNotFoundException_whenCardNotExists() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.empty());

        // Act | Assert
        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardService.block(cardId, ownerId)
        );
        assertEquals("Card with id " + cardId + " not found for owner " + ownerId, ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteCard_whenCardExists() {
        // Arrange
        Long cardId = 1L;

        when(cardRepository.existsById(cardId)).thenReturn(true);

        // Act
        cardService.delete(cardId);

        // Assert
        verify(cardRepository, times(1)).deleteById(cardId);
    }

    @Test
    void delete_shouldDoNothing_whenCardNotExists() {
        // Arrange
        Long cardId = 1L;

        when(cardRepository.existsById(cardId)).thenReturn(false);

        // Act
        cardService.delete(cardId);

        // Assert
        verify(cardRepository, never()).deleteById(cardId);
    }

    @Test
    void deposit_shouldUpdateCardBalance_whenCardExists() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(0.0));
        card.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act
        cardService.deposit(cardId, amount, ownerId);

        // Assert
        assertEquals(BigDecimal.valueOf(100.0), card.getBalance());
        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void deposit_shouldThrowCardNotFoundException_whenCardNotExists() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.empty());

        // Act | Assert
        CardNotFoundException ex = assertThrows(CardNotFoundException.class,
                () -> cardService.deposit(cardId, amount, ownerId)
        );
        assertEquals("Card with id " + cardId + " not found for owner " + ownerId, ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deposit_shouldThrowBusinessLogicException_whenAmountNegative() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;
        BigDecimal amount = BigDecimal.valueOf(-100.0);

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> cardService.deposit(cardId, amount, ownerId)
        );
        assertEquals("Amount must be greater than zero", ex.getMessage());

        verify(cardRepository, never()).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deposit_shouldThrowBusinessLogicException_whenCardBlocked() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);
        card.setBalance(BigDecimal.valueOf(0.0));
        card.setExpirationDate(YearMonth.now().plusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> cardService.deposit(cardId, amount, ownerId)
        );
        assertEquals("Card with id: " + cardId + " non active", ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(card);
    }

    @Test
    void deposit_shouldThrowBusinessLogicException_whenCardExpired() {
        // Arrange
        Long ownerId = 1L;
        Long cardId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100.0);

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(0.0));
        card.setExpirationDate(YearMonth.now().minusMonths(1));

        when(cardRepository.findByIdAndOwnerId(cardId, ownerId)).thenReturn(Optional.of(card));

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> cardService.deposit(cardId, amount, ownerId)
        );
        assertEquals("Card with id: " + cardId + " has expired", ex.getMessage());

        verify(cardRepository, times(1)).findByIdAndOwnerId(cardId, ownerId);
        verify(cardRepository, never()).save(card);
    }
}