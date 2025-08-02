package ru.noleg.bankcards.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Данные для перевода")
public record TransferDto(

        @Schema(description = "Id карты с которой совершается перевод", example = "1")
        @NotNull Long fromCardId,

        @Schema(description = "Id карты на которую совершается перевод", example = "2")
        @NotNull Long toCardId,

        @Schema(description = "Сумма перевода", example = "100.00")
        @NotNull @Positive BigDecimal amount
) {
}
