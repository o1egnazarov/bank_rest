package ru.noleg.bankcards.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.exception.error.BusinessLogicException;
import ru.noleg.bankcards.exception.error.UserNotFoundException;
import ru.noleg.bankcards.repository.UserRepository;
import ru.noleg.bankcards.service.UserService;

import java.util.List;


@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAll(Integer pageNumber, Integer pageSize, Sort sort) {
        Page<User> usersPage = userRepository.findAll(PageRequest.of(pageNumber, pageSize, sort));
        List<User> users = usersPage.getContent();

        logger.debug("Fetched {} users from page {} with size {}", users.size(), pageNumber, pageSize);
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            logger.warn("User not found with ID: {}", id);
            return new UserNotFoundException("User not found with ID: " + id);
        });
    }

    @Override
    public void delete(Long userId) {
        User user = this.userRepository.findById(userId).orElseThrow(() -> {
            logger.warn("User not found with ID: {} for deletion", userId);
            return new UserNotFoundException("User not found with ID: " + userId);
        });

        if (user.getRole() == Role.ROLE_ADMIN) {
            logger.warn("Attempted to delete admin user with ID: {}", userId);
            throw new BusinessLogicException("You can't delete an administrator.");
        }

        this.userRepository.delete(user);
        logger.debug("Deleted user with ID: {}", userId);
    }

    @Override
    public void updateUserRole(Long userId, Role newRole) {
        User user = this.userRepository.findById(userId).orElseThrow(() -> {
            logger.warn("User not found with ID: {} for update role", userId);
            return new UserNotFoundException("User not found with ID: " + userId);
        });
        Role oldRole = user.getRole();

        user.setRole(newRole);
        userRepository.save(user);

        logger.info("Updated role for user ID {}: {} -> {}", userId, oldRole, newRole);
    }
}
