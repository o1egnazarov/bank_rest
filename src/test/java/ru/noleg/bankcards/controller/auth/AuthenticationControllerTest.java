package ru.noleg.bankcards.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.noleg.bankcards.controller.AuthenticationController;
import ru.noleg.bankcards.controller.JwtTestSecurityConfig;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.mapper.UserMapper;
import ru.noleg.bankcards.security.auth.AuthenticationService;
import ru.noleg.bankcards.security.dto.SignIn;
import ru.noleg.bankcards.security.dto.SignUp;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({TestAuthControllerMocksConfig.class, JwtTestSecurityConfig.class})
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;


    @AfterEach
    void resetMocks() {
        Mockito.reset(authenticationService);
    }


    @Test
    void signUp_shouldReturn200AndUserId_whenOk() throws Exception {
        // Arrange
        Long expectedUserID = 42L;

        SignUp signUp = new SignUp(
                "user@example.com",
                "strongPass123",
                "Иван",
                "Иванов",
                "Иванович"
        );

        User user = new User(
                null,
                signUp.email(),
                signUp.password(),
                signUp.firstName(),
                signUp.lastName(),
                signUp.patronymic(),
                Role.ROLE_USER
        );

        when(userMapper.mapToRegisterEntityFromSignUp(signUp)).thenReturn(user);
        when(authenticationService.signUp(user)).thenReturn(expectedUserID);

        // Act | Assert
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUp))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));

        verify(userMapper, times(1)).mapToRegisterEntityFromSignUp(signUp);
        verify(authenticationService, times(1)).signUp(user);
    }

    @Test
    void signUp_shouldReturn400_whenInvalidInput() throws Exception {
        // Arrange
        SignUp invalidSignUp = new SignUp(
                "", "12345678", "Иван", "Иванов", "Иванович"
        );

        // Act | Assert
        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSignUp))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userMapper, never()).mapToRegisterEntityFromSignUp(invalidSignUp);
        verify(authenticationService, never()).signUp(any());
    }

    @Test
    void signIn_shouldReturn200AndJwtToken_whenOk() throws Exception {
        // Arrange
        SignIn signIn = new SignIn("user@example.com", "strongPass123");
        String jwtToken = "mocked.jwt.token";

        when(authenticationService.signIn(signIn.email(), signIn.password()))
                .thenReturn(jwtToken);

        // Act | Assert
        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signIn))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken));

        verify(authenticationService, times(1)).signIn(signIn.email(), signIn.password());
    }

    @Test
    void signIn_shouldReturn400_whenInvalidInput() throws Exception {
        // Arrange
        SignIn invalidSignIn = new SignIn("email@gmail.com", "");

        // Act | Assert
        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSignIn))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).signIn(anyString(), anyString());
    }
}