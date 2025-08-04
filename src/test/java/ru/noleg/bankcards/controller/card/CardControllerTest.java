package ru.noleg.bankcards.controller.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.noleg.bankcards.controller.CardController;
import ru.noleg.bankcards.controller.JwtTestSecurityConfig;
import ru.noleg.bankcards.dto.card.CardDto;
import ru.noleg.bankcards.dto.card.CardSort;
import ru.noleg.bankcards.dto.card.CreateCardDto;
import ru.noleg.bankcards.entity.Card;
import ru.noleg.bankcards.entity.CardStatus;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.mapper.CardMapper;
import ru.noleg.bankcards.security.user.UserDetailsImpl;
import ru.noleg.bankcards.service.CardService;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CardController.class)
@Import({TestCardControllerMocksConfig.class, JwtTestSecurityConfig.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardService cardService;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void resetMocks() {
        Mockito.reset(cardService);
        Mockito.reset(cardMapper);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_shouldReturn201_forAdmin() throws Exception {
        // Arrange
        CreateCardDto dto = new CreateCardDto(
                1L, YearMonth.now().plusMonths(1)
        );
        Card card = new Card();
        when(cardMapper.mapToCardEntity(dto)).thenReturn(card);
        when(cardService.create(card)).thenReturn(42L);

        // Act | Assert
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().string("42"));

        verify(cardService, times(1)).create(card);
        verify(cardMapper, times(1)).mapToCardEntity(dto);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_shouldReturn400_forInvalidRequest() throws Exception {
        // Arrange
        Long ownerId = 1L;
        CreateCardDto dto = new CreateCardDto(
                ownerId, YearMonth.now().minusMonths(1)
        );

        // Act | Assert
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).create(any());
        verify(cardMapper, never()).mapToCardEntity(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_shouldReturn204_forAdmin_whenRequestValid() throws Exception {
        // Arrange
        Long cardId = 1L;
        Long ownerId = 100L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/activate/{cardId}", cardId)
                        .param("ownerId", ownerId.toString())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).activate(cardId, ownerId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void activateCard_shouldReturn403_whenNotAdmin() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/activate/{cardId}", cardId)
                        .param("ownerId", "100"))
                .andExpect(status().isForbidden());

        verify(cardService, never()).activate(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_shouldReturn204_forAdmin_whenRequestValid() throws Exception {
        // Arrange
        Long cardId = 1L;
        Long ownerId = 100L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/block/{cardId}", cardId)
                        .param("ownerId", ownerId.toString())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).block(cardId, ownerId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCard_shouldReturn403_whenNotAdmin() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/block/{cardId}", cardId)
                        .param("ownerId", "100"))
                .andExpect(status().isForbidden());

        verify(cardService, never()).block(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_shouldReturn400_whenCardIdInvalid() throws Exception {
        // Arrange
        Long invalidCardId = 0L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/block/{cardId}", invalidCardId)
                        .param("ownerId", "100")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).block(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCardByUser_shouldReturn204_whenRequestIsValid() throws Exception {
        // Arrange
        Long cardId = 5L;
        Long ownerId = 100L;

        User user = new User();
        user.setId(ownerId);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        // Act | Assert
        mockMvc.perform(post("/api/cards/block/self/{cardId}", cardId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).block(cardId, ownerId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCardByUser_shouldReturn403_whenNotUserRole() throws Exception {
        // Arrange
        Long cardId = 5L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/block/self/{cardId}", cardId))
                .andExpect(status().isForbidden());

        verify(cardService, never()).block(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCardByUser_shouldReturn400_whenCardIdInvalid() throws Exception {
        // Arrange
        Long invalidCardId = 0L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/block/self/{cardId}", invalidCardId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).block(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_shouldReturn204_whenRequestIsValid() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act | Assert
        mockMvc.perform(delete("/api/cards/{cardId}", cardId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService, times(1)).delete(cardId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCard_shouldReturn403_whenNotAdmin() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act | Assert
        mockMvc.perform(delete("/api/cards/{cardId}", cardId))
                .andExpect(status().isForbidden());

        verify(cardService, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_shouldReturn400_whenIdIsInvalid() throws Exception {
        // Arrange
        Long invalidCardId = 0L;

        // Act | Assert
        mockMvc.perform(delete("/api/cards/{cardId}", invalidCardId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deposit_shouldReturn204_whenValidRequest() throws Exception {
        // Arrange
        Long cardId = 1L;
        String amount = "100.50";
        Long ownerId = 100L;

        User user = new User();
        user.setId(ownerId);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        // Act | Assert
        mockMvc.perform(post("/api/cards/deposit/{cardId}", cardId)
                        .param("amount", amount)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardService).deposit(cardId, new BigDecimal(amount), ownerId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deposit_shouldReturn403_whenNotUser() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/deposit/{cardId}", cardId)
                        .param("amount", "50")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cardService, never()).deposit(anyLong(), any(), anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deposit_shouldReturn400_whenAmountIsInvalid() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act | Assert
        mockMvc.perform(post("/api/cards/deposit/{cardId}", cardId)
                        .param("amount", "0")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).deposit(anyLong(), any(), anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalance_shouldReturn200WithBalance_whenValidRequest() throws Exception {
        // Arrange
        Long cardId = 1L;
        Long ownerId = 42L;
        BigDecimal expectedBalance = new BigDecimal("150.75");

        User user = new User();
        user.setId(ownerId);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(cardService.getBalance(cardId, ownerId)).thenReturn(expectedBalance);

        // Act | Assert
        mockMvc.perform(get("/api/cards/{cardId}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedBalance.toString()));

        verify(cardService, times(1)).getBalance(cardId, ownerId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalance_shouldReturn400_whenCardIdIsInvalid() throws Exception {
        // Arrange
        Long invalidCardId = 0L;
        Long ownerId = 42L;

        User user = new User();
        user.setId(ownerId);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        // Act | Assert
        mockMvc.perform(get("/api/cards/{cardId}/balance", invalidCardId))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).getBalance(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBalance_shouldReturn403_whenNotUser() throws Exception {
        // Arrange
        Long cardId = 1L;
        Long adminId = 1L;

        User user = new User();
        user.setId(adminId);
        user.setRole(Role.ROLE_ADMIN);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        // Act | Assert
        mockMvc.perform(get("/api/cards/{cardId}/balance", cardId))
                .andExpect(status().isForbidden());

        verify(cardService, never()).getBalance(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCard_shouldReturn200WithCardDto_whenCardExists() throws Exception {
        // Arrange
        Long cardId = 1L;
        Long ownerId = 42L;

        Card card = new Card();
        card.setId(cardId);

        User user = new User();
        user.setId(ownerId);
        card.setOwner(user);

        CardDto dto = new CardDto(
                "**** **** **** 3456",
                ownerId,
                YearMonth.of(2023, 1),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00")
        );

        when(cardService.get(cardId)).thenReturn(card);
        when(cardMapper.mapToCardDto(card)).thenReturn(dto);

        // Act | Assert
        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(ownerId));

        verify(cardService, times(1)).get(cardId);
        verify(cardMapper, times(1)).mapToCardDto(card);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCard_shouldReturn400_whenIdIsInvalid() throws Exception {
        // Arrange
        Long invalidCardId = 0L;

        // Act | Assert
        mockMvc.perform(get("/api/cards/{id}", invalidCardId))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).get(anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_shouldReturn403_whenNotAdmin() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act | Assert
        mockMvc.perform(get("/api/cards/{id}", cardId))
                .andExpect(status().isForbidden());

        verify(cardService, never()).get(anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCardsByOwner_shouldReturn200WithList_whenValidRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        CardSort sort = CardSort.DATE_ASC;
        Integer pageNumber = 0;
        Integer pageSize = 2;

        User user = new User();
        user.setId(userId);

        Long card1Id = 1L;
        Card card1 = new Card();
        card1.setId(card1Id);
        card1.setOwner(user);

        Long card2Id = 2L;
        Card card2 = new Card();
        card2.setId(card2Id);
        card2.setOwner(user);

        List<Card> cards = List.of(card1, card2);

        CardDto cardDto1 = new CardDto(
                "**** **** **** 1234",
                userId,
                YearMonth.of(2023, 1),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00")
        );

        CardDto cardDto2 = new CardDto(
                "**** **** **** 3456",
                userId,
                YearMonth.of(2023, 2),
                CardStatus.ACTIVE,
                new BigDecimal("1500.00")
        );
        List<CardDto> dtos = List.of(cardDto1, cardDto2);

        when(cardService.getAllByOwner(userId, pageNumber, pageSize, sort.getSortValue()))
                .thenReturn(cards);
        when(cardMapper.mapToCardDtos(cards)).thenReturn(dtos);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(get("/api/cards/owner/me")
                        .param("pageNumber", "0")
                        .param("pageSize", "2")
                        .param("sort", "DATE_ASC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ownerId").value(1))
                .andExpect(jsonPath("$[1].ownerId").value(1));

        verify(cardService, times(1))
                .getAllByOwner(userId, pageNumber, pageSize, sort.getSortValue());
        verify(cardMapper, times(1)).mapToCardDtos(cards);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCardsByOwner_shouldReturn400_whenInvalidPaginationParams() throws Exception {
        mockMvc.perform(get("/api/cards/owner/me")
                        .param("pageNumber", "-1")
                        .param("pageSize", "20")
                        .param("sort", "DATE_ASC"))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).getAllByOwner(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_shouldReturn200WithList_whenValidRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        CardSort sort = CardSort.DATE_ASC;
        Integer pageNumber = 0;
        Integer pageSize = 2;

        User user = new User();
        user.setId(userId);

        Long card1Id = 1L;
        Card card1 = new Card();
        card1.setId(card1Id);
        card1.setOwner(user);

        Long card2Id = 2L;
        Card card2 = new Card();
        card2.setId(card2Id);
        card2.setOwner(user);

        List<Card> cards = List.of(card1, card2);

        CardDto cardDto1 = new CardDto(
                "**** **** **** 1234",
                userId,
                YearMonth.of(2023, 1),
                CardStatus.ACTIVE,
                new BigDecimal("1000.00")
        );

        CardDto cardDto2 = new CardDto(
                "**** **** **** 3456",
                userId,
                YearMonth.of(2023, 2),
                CardStatus.ACTIVE,
                new BigDecimal("1500.00")
        );
        List<CardDto> dtos = List.of(cardDto1, cardDto2);

        when(cardService.getAll(pageNumber, pageSize, sort.getSortValue()))
                .thenReturn(cards);
        when(cardMapper.mapToCardDtos(cards)).thenReturn(dtos);

        mockMvc.perform(get("/api/cards")
                        .param("pageNumber", "0")
                        .param("pageSize", "2")
                        .param("sort", "DATE_ASC")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ownerId").value(1))
                .andExpect(jsonPath("$[1].ownerId").value(1));

        verify(cardService, times(1))
                .getAll(pageNumber, pageSize, sort.getSortValue());
        verify(cardMapper, times(1)).mapToCardDtos(cards);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_shouldReturn400_whenInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .param("pageNumber", "-1")
                        .param("pageSize", "50")
                        .param("sort", "DATE_ASC"))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).getAll(anyInt(), anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .param("pageNumber", "0")
                        .param("pageSize", "3")
                        .param("sort", "DATE_ASC"))
                .andExpect(status().isForbidden());
    }
}