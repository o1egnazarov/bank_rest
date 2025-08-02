package ru.noleg.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Профиль пользователя")
public record ProfileDto(

        @Schema(description = "Имя пользователя", example = "Иван")
        String firstName,

        @Schema(description = "Фамилия пользователя", example = "Иванов")
        String lastName,

        @Schema(description = "Отчество пользователя", example = "Иванович")
        String patronymic,

        @Schema(description = "Email пользователя", example = "example@gmail.com")
        String email
) {
}
