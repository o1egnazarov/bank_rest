package ru.noleg.bankcards.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.noleg.bankcards.controller.AdminController;
import ru.noleg.bankcards.controller.JwtTestSecurityConfig;
import ru.noleg.bankcards.dto.user.UserDto;
import ru.noleg.bankcards.dto.user.UserSort;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.mapper.UserMapper;
import ru.noleg.bankcards.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdminController.class)
@Import({TestUserControllerMocksConfig.class, JwtTestSecurityConfig.class})
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnUserDtoList() throws Exception {
        // Arrange
        User user1 = new User(
                1L,
                "email1",
                "password1",
                "Иван",
                "Иванов",
                "Иванович",
                Role.ROLE_USER
        );
        User user2 = new User(
                1L,
                "email2",
                "password2",
                "Петр",
                "Петров",
                "Петрович",
                Role.ROLE_ADMIN
        );
        List<User> users = List.of(user1, user2);

        UserDto userDto1 = new UserDto(
                1L,
                "Иван",
                "Иванов",
                "Иванович",
                "email1",
                Role.ROLE_USER
        );
        UserDto userDto2 = new UserDto(
                2L,
                "Петр",
                "Петров",
                "Петрович",
                "email2",
                Role.ROLE_ADMIN
        );
        List<UserDto> dtos = List.of(userDto1, userDto2);

        UserSort sort = UserSort.LAST_NAME_ASC;
        when(userService.getAll(0, 2, sort.getSortValue())).thenReturn(users);
        when(userMapper.mapToUserDtos(users)).thenReturn(dtos);

        // Act | Assert
        mockMvc.perform(get("/api/admin/users")
                        .param("pageNumber", "0")
                        .param("pageSize", "2")
                        .param("sort", "LAST_NAME_ASC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Иван"))
                .andExpect(jsonPath("$[1].email").value("email2"));

        verify(userService, times(1)).getAll(0, 2, sort.getSortValue());
        verify(userMapper, times(1)).mapToUserDtos(users);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturn403_ifWrongRole() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .param("pageNumber", "0")
                        .param("pageSize", "2")
                        .param("sort", "LAST_NAME_ASC"))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAll(anyInt(), anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturn400_ifInvalidParams() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .param("pageNumber", "-1")
                        .param("pageSize", "100")
                        .param("sort", "LAST_NAME_ASC"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getAll(anyInt(), anyInt(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturn200_whenUserIsAdmin() throws Exception {
        // Arrange
        doNothing().when(userService).delete(1L);

        // Act | Assert
        mockMvc.perform(delete("/api/admin/users/{userId}", 1).with(csrf()))
                .andExpect(status().isOk());

        verify(userService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturn400_ifUserIdInvalid() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{userId}", 0).with(csrf()))
                .andExpect(status().isBadRequest());

        verify(userService, never()).delete(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_shouldReturn200_whenUserIsAdmin() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}", 1)
                        .param("role", "ROLE_ADMIN")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).updateUserRole(1L, Role.ROLE_ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_shouldReturn400_ifMissingRoleParam() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}", 1).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_shouldReturn400_ifInvalidUserId() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}", -10).with(csrf())
                        .param("role", "ROLE_USER"))
                .andExpect(status().isBadRequest());
    }
}