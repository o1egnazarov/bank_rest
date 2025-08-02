package ru.noleg.bankcards.exception.handler;

import java.time.LocalDateTime;

public record ExceptionResponse(
        ErrorCode code,
        String message,
        String path,
        LocalDateTime timestamp
) {
}