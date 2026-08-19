package com.raileasy.security;

import com.raileasy.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-long-enough-for-hs256-signing";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
        user = new User("rider@raileasy.com", "hashed", "Rider", false);
        // id is normally assigned by JPA on persist; set it manually for this unit test
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    @Test
    void generateToken_producesTokenContainingSubjectEmail() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo("rider@raileasy.com");
    }

    @Test
    void isTokenValid_returnsTrue_forFreshlyIssuedToken() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(SECRET, 1L);
        String token = shortLivedJwtService.generateToken(user);
        Thread.sleep(10);

        assertThat(shortLivedJwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-valid-token")).isFalse();
    }
}
