package ru.noleg.bankcards.service;

import org.springframework.data.domain.Sort;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;

import java.util.List;

public interface UserService {

    List<User> getAll(Integer pageNumber, Integer pageSize, Sort sort);

    User get(Long id);

    void delete(Long userId);

    void updateUserRole(Long userId, Role role);
}
