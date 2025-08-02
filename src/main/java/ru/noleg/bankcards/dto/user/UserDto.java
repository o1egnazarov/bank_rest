package ru.noleg.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.noleg.bankcards.entity.Role;

@Schema(description = "Пользователь")
public record UserDto(

        @Schema(description = "Id пользователя", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @Schema(description = "Имя пользователя", example = "Иван")
        String firstName,

        @Schema(description = "Фамилия пользователя", example = "Иванов")
        String lastName,

        @Schema(description = "Отчество пользователя", example = "Иванович")
        String patronymic,

        @Schema(description = "Email пользователя", example = "example@gmail.com")
        String email,

        @Schema(description = "Роль пользователя", example = "ROLE_USER")
        Role role
) {
}