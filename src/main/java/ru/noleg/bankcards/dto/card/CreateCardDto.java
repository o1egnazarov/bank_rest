package ru.noleg.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.noleg.bankcards.validator.annotation.NotPastYearMonth;

import java.time.YearMonth;

@Schema(description = "Данные для создания карты")
public record CreateCardDto(

        @Schema(description = "Id владельца карты", example = "1")
        @NotNull Long ownerId,

        @Schema(description = "Дата окончания срока действия карты", example = "2023-12")
        @NotNull @NotPastYearMonth YearMonth expirationDate
) {
}