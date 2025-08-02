package ru.noleg.bankcards.exception.handler;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.noleg.bankcards.exception.error.BusinessLogicException;
import ru.noleg.bankcards.exception.error.CardNotFoundException;
import ru.noleg.bankcards.exception.error.CardTransferException;
import ru.noleg.bankcards.exception.error.UserNotFoundException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleCardNotFoundException(CardNotFoundException ex, HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ErrorCode.RECOURSE_NOT_FOUND,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(CardTransferException.class)
    public ResponseEntity<ExceptionResponse> handleCardTransferException(CardTransferException ex, HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                ErrorCode.BUSINESS_LOGIC_ERROR,
                request.getRequestURI(),
                ex
        );
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ErrorCode.USER_NOT_FOUND,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ExceptionResponse> handleBusinessLogicException(BusinessLogicException ex, HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                ErrorCode.BUSINESS_LOGIC_ERROR,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleSqlException(DataIntegrityViolationException ex, HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.BAD_REQUEST,
                "Probably an attempt to delete an entity associated with another entity.",
                ErrorCode.DATABASE_ERROR,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingRequestParamException(MissingServletRequestParameterException ex,
                                                                                HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorCode.MISSING_REQUIRED_PARAM,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException ex,
                                                                                HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorCode.VALIDATION_ERROR,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                   HttpServletRequest request) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return this.buildResponse(
                HttpStatus.BAD_REQUEST,
                errors,
                ErrorCode.VALIDATION_ERROR,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleCommonException(Exception ex, HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                ErrorCode.UNKNOWN_ERROR,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid login or password",
                ErrorCode.UNAUTHORIZED,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {
        return this.buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                ErrorCode.NO_ACCESS_TO_RECOURSE,
                request.getRequestURI(),
                ex
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access denied: insufficient rights",
                ErrorCode.NO_ACCESS_TO_RECOURSE,
                request.getRequestURI(),
                ex
        );
    }

    private ResponseEntity<ExceptionResponse> buildResponse(HttpStatus status,
                                                            String message,
                                                            ErrorCode errorCode,
                                                            String path,
                                                            Throwable ex) {
        if (ex != null) {
            logger.error("Exception occurred: {}.", ex.getMessage(), ex);
        }
        return ResponseEntity.status(status)
                .body(new ExceptionResponse(
                        errorCode,
                        message,
                        path,
                        LocalDateTime.now())
                );
    }
}