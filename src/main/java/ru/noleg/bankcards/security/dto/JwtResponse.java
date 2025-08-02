package ru.noleg.bankcards.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ при аутентификации")
public record JwtResponse(
        @Schema(description = "Jwt токен") String token
) {
}