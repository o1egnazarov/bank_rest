package ru.noleg.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.noleg.bankcards.dto.user.ProfileDto;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.mapper.UserMapper;
import ru.noleg.bankcards.security.user.UserDetailsImpl;
import ru.noleg.bankcards.service.UserService;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Контроллер для пользователя.",
        description = "Позволяет управлять пользователями (обновлять/удалять/получать)."
)
@SecurityRequirement(name = "JWT")
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Получение профиля пользователя.",
            description = "Позволяет получить текущую информацию о профиле пользователя."
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProfileDto> getUserProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long id = userDetails.getId();
        logger.info("Request: GET /me fetching profile for user with id: {}.", id);

        User user = this.userService.get(id);

        logger.info("Profile for user with id: {} successfully fetched.", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.userMapper.mapToProfileDto(user));
    }
}

