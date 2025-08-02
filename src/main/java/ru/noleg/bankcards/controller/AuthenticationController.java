package ru.noleg.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.mapper.UserMapper;
import ru.noleg.bankcards.security.auth.AuthenticationService;
import ru.noleg.bankcards.security.dto.JwtResponse;
import ru.noleg.bankcards.security.dto.SignIn;
import ru.noleg.bankcards.security.dto.SignUp;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Контроллер для регистрации/аутентификации.",
        description = "Позволяет зарегистрироваться новому пользователю или повторно войти в систему."
)
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    public AuthenticationController(AuthenticationService authenticationService, UserMapper userMapper) {
        this.authenticationService = authenticationService;
        this.userMapper = userMapper;
    }

    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация пользователя.",
            description = "Позволяет зарегистрироваться новому пользователю."
    )
    public ResponseEntity<Long> signUp(
            @RequestBody @Valid SignUp signUpRequest
    ) {
        logger.info("Request: POST /signUp registration user with email: {}.", signUpRequest.email());

        User user = this.userMapper.mapToRegisterEntityFromSignUp(signUpRequest);
        Long userId = this.authenticationService.signUp(user);

        logger.info("User with email: {} successfully registered.", signUpRequest.email());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userId);
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Аутентификация пользователя.",
            description = "Позволяет повторно войти уже зарегистрированному пользователю."
    )
    public ResponseEntity<JwtResponse> signIn(
            @RequestBody @Valid SignIn signInRequest
    ) {
        logger.info("Request: POST /signIn authentication user with email: {}.", signInRequest.email());

        String token = this.authenticationService.signIn(signInRequest.email(), signInRequest.password());

        logger.info("User with email: {} successfully authenticated.", signInRequest.email());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new JwtResponse(token));
    }
}