package ru.noleg.bankcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.noleg.bankcards.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
