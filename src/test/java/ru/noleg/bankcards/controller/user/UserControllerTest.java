package ru.noleg.bankcards.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.noleg.bankcards.controller.JwtTestSecurityConfig;
import ru.noleg.bankcards.controller.UserController;
import ru.noleg.bankcards.dto.user.ProfileDto;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.mapper.UserMapper;
import ru.noleg.bankcards.security.user.UserDetailsImpl;
import ru.noleg.bankcards.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({TestUserControllerMocksConfig.class, JwtTestSecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @WithMockUser(roles = "USER")
    void getUserProfile_shouldReturnProfileDto() throws Exception {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setEmail("example@gmail.com");
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setPatronymic("Иванович");
        user.setRole(Role.ROLE_USER);

        ProfileDto expectedDto = new ProfileDto(
                "Иван", "Иванов", "Иванович", "example@gmail.com"
        );

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(userService.get(userId)).thenReturn(user);
        when(userMapper.mapToProfileDto(user)).thenReturn(expectedDto);

        // Act | Assert
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(expectedDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(expectedDto.lastName()))
                .andExpect(jsonPath("$.patronymic").value(expectedDto.patronymic()))
                .andExpect(jsonPath("$.email").value(expectedDto.email()));

        verify(userService, times(1)).get(userId);
        verify(userMapper, times(1)).mapToProfileDto(user);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserProfile_shouldReturnForbidden_whenUserHasWrongRole() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setPassword("secret");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setRole(Role.ROLE_ADMIN);

        UserDetailsImpl principal = new UserDetailsImpl(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        // Act | Assert
        mockMvc.perform(get("/api/users/me")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
