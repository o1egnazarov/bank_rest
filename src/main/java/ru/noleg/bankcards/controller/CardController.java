package ru.noleg.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.noleg.bankcards.dto.card.CardDto;
import ru.noleg.bankcards.dto.card.CardSort;
import ru.noleg.bankcards.dto.card.CreateCardDto;
import ru.noleg.bankcards.entity.Card;
import ru.noleg.bankcards.mapper.CardMapper;
import ru.noleg.bankcards.security.user.UserDetailsImpl;
import ru.noleg.bankcards.service.CardService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(
        name = "Контроллер для карт.",
        description = "Позволяет управлять банковскими картами (обновлять/удалять/получать)."
)
@Validated
@SecurityRequirement(name = "JWT")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    private final CardService cardService;
    private final CardMapper cardMapper;

    public CardController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @PostMapping()
    @Operation(
            summary = "Создание карты.",
            description = "Позволяет создать новую банковскую карту."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createCard(
            @Valid @RequestBody CreateCardDto cardDto
    ) {
        logger.info("Request: POST create card for user with id: {}.", cardDto.ownerId());

        Card card = cardMapper.mapToCardEntity(cardDto);
        Long cardId = cardService.create(card);

        logger.info("Card with id {} successfully created.", cardId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardId);
    }

    @PostMapping("/activate/{cardId}")
    @Operation(
            summary = "Активация карты.",
            description = "Позволяет активировать банковскую карту."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(
            @Parameter(description = "Идентификатор карты", required = true) @Min(1) @PathVariable Long cardId,
            @Parameter(description = "Идентификатор владельца карты", required = true) @Min(1) @RequestParam Long ownerId
    ) {
        logger.info("Request: POST activate card with id: {} for user with id: {}.", cardId, ownerId);

        cardService.activate(cardId, ownerId);

        logger.info("Card with id {} successfully activated.", cardId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/block/{cardId}")
    @Operation(
            summary = "Блокировка карты.",
            description = "Позволяет заблокировать банковскую карту."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCard(
            @Parameter(description = "Идентификатор карты", required = true) @Min(1) @PathVariable Long cardId,
            @Parameter(description = "Идентификатор владельца карты", required = true) @Min(1) @RequestParam Long ownerId
    ) {
        logger.info("Request by admin: POST block card with id: {} for user with id: {}.", cardId, ownerId);

        cardService.block(cardId, ownerId);

        logger.info("Card with id {} successfully blocked by admin.", cardId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/block/self/{cardId}")
    @Operation(
            summary = "Блокировка карты пользователем.",
            description = "Позволяет заблокировать банковскую карту."
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> blockCardByUser(
            @Parameter(description = "Идентификатор карты", required = true) @Min(1) @PathVariable Long cardId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long ownerId = userDetails.getId();
        logger.info("Request: POST block card with id: {} for user with id: {}.", cardId, ownerId);

        cardService.block(cardId, ownerId);

        logger.info("Card with id {} successfully blocked.", cardId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удаление карты.",
            description = "Позволяет удалить существующую банковскую карту."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "Идентификатор карты", required = true) @Min(1) @PathVariable Long id
    ) {
        logger.info("Request: DELETE delete card with id: {}.", id);

        cardService.delete(id);

        logger.info("Card with id {} successfully deleted.", id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PostMapping("/deposit/{cardId}")
    @Operation(
            summary = "Пополнение карты.",
            description = "Позволяет пополнить банковскую карту."
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deposit(
            @Parameter(description = "Идентификатор карты", required = true) @Min(1) @PathVariable Long cardId,
            @Parameter(description = "Сумма пополнения карты", required = true) @Min(1) @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long ownerId = userDetails.getId();
        logger.info("Request: POST deposit {} units to card with ownerId: {} for user with ownerId: {}.", amount, cardId, ownerId);

        cardService.deposit(cardId, amount, ownerId);

        logger.info("Deposit {} units to card with ownerId: {} successfully completed.", amount, cardId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{cardId}/balance")
    @Operation(
            summary = "Получение баланса карты.",
            description = "Позволяет получить баланс банковской карты."
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(description = "Идентификатор карты", required = true) @Min(1) @PathVariable Long cardId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        logger.info("Request: GET balance for card with id: {} for user with id: {}.", cardId, userDetails.getId());
        Long ownerId = userDetails.getId();

        BigDecimal balance = cardService.getBalance(cardId, ownerId);

        logger.info("Balance for card with id {} successfully fetched for user with id {}.", cardId, ownerId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(balance);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получение карты по id.",
            description = "Позволяет получить существующую банковскую карту по id."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> getCard(
            @Parameter(description = "Идентификатор карты", required = true) @Min(1) @PathVariable Long id
    ) {
        logger.info("Request: GET card with id: {}.", id);

        Card card = cardService.get(id);

        logger.info("Card with id {} successfully fetched.", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cardMapper.mapToCardDto(card));
    }

    @GetMapping("/owner/me")
    @Operation(
            summary = "Получение своих карт пользователем.",
            description = "Позволяет получить все существующие банковские карты владельца."
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CardDto>> getAllCardsByOwner(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "Номер страницы") @Min(0) @RequestParam Integer pageNumber,
            @Parameter(description = "Размер страницы") @Min(1) @Max(10) @RequestParam Integer pageSize,
            @Parameter(description = "Сортировка") @RequestParam CardSort sort
    ) {
        logger.info("Request: GET cards for user with id: {}.", userDetails.getId());

        Long ownerId = userDetails.getId();
        List<Card> cards = cardService.getAllByOwner(ownerId, pageNumber, pageSize, sort.getSortValue());

        logger.info("Cards for user with id {} successfully fetched.", ownerId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cardMapper.mapToCardDtos(cards));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(
            summary = "Получение карт владельца.",
            description = "Позволяет получить все существующие банковские карты владельца."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> getAllCardsByOwner(
            @Parameter(description = "Идентификатор владельца карты", required = true) @Min(1) @PathVariable Long ownerId,
            @Parameter(description = "Номер страницы") @Min(0) @RequestParam Integer pageNumber,
            @Parameter(description = "Размер страницы") @Min(1) @Max(10) @RequestParam Integer pageSize,
            @Parameter(description = "Сортировка") @RequestParam CardSort sort
    ) {
        logger.info("Request: GET by admin: get cards for user with id: {}.", ownerId);

        List<Card> cards = cardService.getAllByOwner(ownerId, pageNumber, pageSize, sort.getSortValue());

        logger.info("Cards for user with id {} successfully fetched by admin.", ownerId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cardMapper.mapToCardDtos(cards));
    }

    @GetMapping()
    @Operation(
            summary = "Получение всех карт.",
            description = "Позволяет получить все существующие банковские карты."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> getAllCards(
            @Parameter(description = "Номер страницы") @Min(0) @RequestParam Integer pageNumber,
            @Parameter(description = "Размер страницы") @Min(1) @Max(10) @RequestParam Integer pageSize,
            @Parameter(description = "Сортировка") @RequestParam CardSort sort
    ) {
        logger.info("Request: GET fetching all cards.");

        List<Card> cards = cardService.getAll(pageNumber, pageSize, sort.getSortValue());

        logger.info("Got {} cards.", cards.size());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cardMapper.mapToCardDtos(cards));
    }
}
