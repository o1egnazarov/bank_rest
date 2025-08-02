package ru.noleg.bankcards.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Аутентификация пользователя")
public record SignIn(
        @Schema(description = "Уникальный email пользователя", example = "user123@gmail.com")
        @NotBlank @Email @Size(min = 5, max = 50)
        String email,

        @NotBlank @Schema(description = "Пароль пользователя", example = "user_password123")
        String password
) {
}