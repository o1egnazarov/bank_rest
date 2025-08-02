package ru.noleg.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.noleg.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "Банковская карта")
public record CardDto(

        @Schema(description = "Номер карты", example = "**** **** **** 1234")
        String maskedNumber,

        @Schema(description = "Id владельца карты", example = "1")
        Long ownerId,

        @Schema(description = "Дата окончания действия карты", example = "2023-12")
        YearMonth expirationDate,

        @Schema(description = "Статус карты", example = "ACTIVE")
        CardStatus status,

        @Schema(description = "Баланс карты", example = "1000.00")
        BigDecimal balance
) {
}
