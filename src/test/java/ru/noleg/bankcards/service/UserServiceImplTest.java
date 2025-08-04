package ru.noleg.bankcards.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.exception.error.BusinessLogicException;
import ru.noleg.bankcards.exception.error.UserNotFoundException;
import ru.noleg.bankcards.repository.UserRepository;
import ru.noleg.bankcards.service.impl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void delete_shouldDeleteUser_whenUserExistsAndIsNotAdmin() {
        // Arrange
        Long userId = 1L;
        User user = mock(User.class);
        when(user.getRole()).thenReturn(Role.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.delete(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenUserNotFound() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act | Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.delete(userId));
        assertEquals("User not found with ID: 1", ex.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_shouldThrowBusinessLogicException_whenUserIsAdmin() {
        // Arrange
        Long userId = 1L;
        User adminUser = new User();
        adminUser.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // Act | Assert
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
                () -> this.userService.delete(userId));
        assertEquals("You can't delete an administrator.", ex.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        // Arrange
        int pageNumber = 1;
        int pageSize = 10;
        Sort sort = mock(Sort.class);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        List<User> userList = List.of(mock(User.class), mock(User.class));
        Page<User> users = new PageImpl<>(userList, pageRequest, userList.size());
        when(userRepository.findAll(pageRequest)).thenReturn(users);

        // Act
        List<User> result = userService.getAll(pageNumber, pageSize, sort);

        // Assert
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getUser_shouldReturnUser_whenUserExists() {
        // Arrange
        Long userId = 1L;
        User user = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User result = userService.get(userId);

        // Assert
        assertEquals(user, result);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUser_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act | Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.get(userId));
        assertEquals("User not found with ID: 1", ex.getMessage());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUserRole_shouldUpdateRole_whenUserExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setRole(Role.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.updateUserRole(userId, Role.ROLE_ADMIN);

        // Assert
        assertEquals(Role.ROLE_ADMIN, user.getRole());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserRole_shouldThrowUserNotFoundException_whenUserNotExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setRole(Role.ROLE_USER);

        // Act | Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.updateUserRole(userId, Role.ROLE_ADMIN));
        assertEquals("User not found with ID: 1", ex.getMessage());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(user);
    }
}