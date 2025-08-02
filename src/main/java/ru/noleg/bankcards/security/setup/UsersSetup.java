package ru.noleg.bankcards.security.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.noleg.bankcards.entity.Role;
import ru.noleg.bankcards.entity.User;
import ru.noleg.bankcards.repository.UserRepository;

@Component
@Transactional
public class UsersSetup implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UsersSetup.class);

    private static final String ADMIN_NAME = "admin";
    private static final String ADMIN_SURNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    private static final String USER_NAME = "Ivan";
    private static final String USER_SURNAME = "Ivanov";
    private static final String USER_PATRONYMIC = "Ivanovich";
    private static final String USER_EMAIL = "user@gmail.com";

    @Value("${app.admin.password:admin123}")
    private String ADMIN_PASSWORD;
    @Value("${app.user.password:user123}")
    private String USER_PASSWORD;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersSetup(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        this.initUser(ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_NAME, ADMIN_SURNAME, null, Role.ROLE_ADMIN);
        this.initUser(USER_EMAIL, USER_PASSWORD, USER_NAME, USER_SURNAME, USER_PATRONYMIC, Role.ROLE_USER);
    }

    private void initUser(String email, String password, String name, String surname, String patronymic, Role role) {

        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Email '{}' already used.", email);
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(name);
        user.setLastName(surname);
        user.setPatronymic(patronymic);
        user.setRole(role);

        userRepository.save(user);
        logger.info("Created default user: '{}'.", email);
    }
}