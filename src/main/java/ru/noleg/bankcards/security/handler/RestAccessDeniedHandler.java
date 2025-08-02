package ru.noleg.bankcards.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import ru.noleg.bankcards.exception.handler.ErrorCode;
import ru.noleg.bankcards.exception.handler.ExceptionResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(this.objectMapper.writeValueAsString(
                new ExceptionResponse(
                        ErrorCode.NO_ACCESS_TO_RECOURSE,
                        "Access denied: insufficient rights",
                        request.getRequestURI(),
                        LocalDateTime.now()
                )
        ));
    }
}