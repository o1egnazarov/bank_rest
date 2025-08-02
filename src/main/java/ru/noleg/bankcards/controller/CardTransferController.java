package ru.noleg.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.noleg.bankcards.dto.transfer.TransferDto;
import ru.noleg.bankcards.security.user.UserDetailsImpl;
import ru.noleg.bankcards.service.CardTransferService;

@RestController
@RequestMapping("/api/transfer")
@Tag(
        name = "Контроллер для переводов.",
        description = "Позволяет переводить деньги между картами одного пользователя."
)
@SecurityRequirement(name = "JWT")
public class CardTransferController {

    private static final Logger logger = LoggerFactory.getLogger(CardTransferController.class);

    private final CardTransferService cardTransferService;

    public CardTransferController(CardTransferService cardTransferService) {
        this.cardTransferService = cardTransferService;
    }

    @PostMapping()
    @Operation(
            summary = "Перевод средств.",
            description = "Позволяет перевести средства между картами пользователя."
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transfer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody TransferDto transferDto
    ) {
        Long ownerId = userDetails.getId();

        logger.info("Request: POST /transfer: User {} wants to transfer {} from card {} to card {}",
                ownerId, transferDto.amount(), transferDto.fromCardId(), transferDto.toCardId()
        );

        cardTransferService.transfer(ownerId, transferDto.fromCardId(), transferDto.toCardId(), transferDto.amount());

        logger.info("Transfer from card {} to card {} successfully completed.",
                transferDto.fromCardId(), transferDto.toCardId()
        );
        return ResponseEntity
                .noContent()
                .build();
    }
}
