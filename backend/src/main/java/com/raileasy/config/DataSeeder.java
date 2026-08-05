package com.raileasy.config;

import com.raileasy.user.User;
import com.raileasy.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the admin user at startup (idempotent). The password is hashed with the
 * real {@link PasswordEncoder}, so no hardcoded BCrypt hash is needed.
 *
 * <p>Trains and schedules are seeded via {@code data.sql}.
 *
 * <p>Default admin credentials: {@code admin@raileasy.com} / {@code Admin@123}.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String ADMIN_EMAIL = "admin@raileasy.com";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            User admin = new User(
                    ADMIN_EMAIL,
                    passwordEncoder.encode(ADMIN_PASSWORD),
                    "Admin",
                    true
            );
            userRepository.save(admin);
            log.info("Seeded admin user: {} (password: {})", ADMIN_EMAIL, ADMIN_PASSWORD);
        }
    }
}
