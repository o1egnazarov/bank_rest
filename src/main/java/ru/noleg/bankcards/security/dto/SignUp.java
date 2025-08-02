package ru.noleg.bankcards.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Регистрация пользователя")
public record SignUp(
        @Schema(description = "Уникальный email пользователя", example = "user123@gmail.com")
        @NotBlank @Email @Size(min = 5, max = 50)
        String email,

        @NotBlank @Schema(description = "Пароль пользователя", example = "user_password123")
        String password,

        @NotBlank @Size(min = 5, max = 50) @Schema(description = "Имя пользователя", example = "Иван")
        String firstName,

        @NotBlank @Size(min = 5, max = 50) @Schema(description = "Фамилия пользователя", example = "Иванов")
        String lastName,

        @Size(max = 50) @Schema(description = "Отчество пользователя", example = "Иванович")
        String patronymic
) {
}