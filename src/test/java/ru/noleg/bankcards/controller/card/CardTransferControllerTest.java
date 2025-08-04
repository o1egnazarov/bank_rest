package ru.noleg.bankcards.controller.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.noleg.bankcards.controller.CardTransferController;
import ru.noleg.bankcards.controller.JwtTestSecurityConfig;
import ru.noleg.bankcards.dto.transfer.TransferDto;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.security.user.UserDetailsImpl;
import ru.noleg.bankcards.service.CardTransferService;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardTransferController.class)
@Import({TestTransferCardControllerMocksConfig.class, JwtTestSecurityConfig.class})
class CardTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardTransferService cardTransferService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void transfer_shouldReturn204_whenTransferIsSuccessful() throws Exception {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100.00);

        TransferDto transferDto = new TransferDto(fromCardId, toCardId, amount);

        User user = new User();
        Long userId = 10L;
        user.setId(userId);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        // Act | Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cardTransferService, times(1))
                .transfer(userId, fromCardId, toCardId, amount);
    }

    @Test
    @WithMockUser(roles = "USER")
    void transfer_shouldReturn400_whenInputIsInvalid() throws Exception {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal negativeAmount = BigDecimal.valueOf(-10);

        TransferDto invalidDto = new TransferDto(fromCardId, toCardId, negativeAmount);

        // Act | Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardTransferService, never()).transfer(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void transfer_shouldReturn403_whenUserHasNoRoleUser() throws Exception {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100.00);

        TransferDto transferDto = new TransferDto(fromCardId, toCardId, amount);

        // Act | Assert
        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isForbidden());

        verify(cardTransferService, never()).transfer(anyLong(), anyLong(), anyLong(), any());
    }
}