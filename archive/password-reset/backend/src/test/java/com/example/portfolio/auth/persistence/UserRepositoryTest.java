package com.example.portfolio.auth.persistence;

import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_auth;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // ── findByEmail ───────────────────────────────────────────────────────────

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        User user = User.builder()
                .role(UserRole.USER)
                .email("admin@test.ro")
                .password("encoded_password")
                .build();
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@test.ro");
        assertThat(result.get().getRole()).isEqualTo(UserRole.USER);
    }

    // ── existsByEmail ─────────────────────────────────────────────────────────

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.USER);

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("user@test.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("missing@test.com");

        assertThat(exists).isFalse();
    }

    // ── findByPasswordResetToken ──────────────────────────────────────────────

    @Test
    void findByPasswordResetToken_shouldReturnUser_whenTokenExists() {
        User user = User.builder()
                .email("reset@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .passwordResetToken("xyz-reset-token")
                .passwordResetTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();
        userRepository.save(user);

        Optional<User> result = userRepository.findByPasswordResetToken("xyz-reset-token");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("reset@test.com");
        assertThat(result.get().getPasswordResetToken()).isEqualTo("xyz-reset-token");
    }

    @Test
    void findByPasswordResetToken_shouldReturnEmpty_whenTokenDoesNotExist() {
        Optional<User> result = userRepository.findByPasswordResetToken("unknown-token");

        assertThat(result).isEmpty();
    }

    // ── findByRefreshToken ────────────────────────────────────────────────────

    @Test
    void findByRefreshToken_shouldReturnUser_whenTokenExists() {
        User user = User.builder()
                .email("refresh@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .refreshToken("qrs-refresh-token")
                .refreshTokenExpiry(LocalDateTime.now().plusDays(7))
                .build();
        userRepository.save(user);

        Optional<User> result = userRepository.findByRefreshToken("qrs-refresh-token");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("refresh@test.com");
        assertThat(result.get().getRefreshToken()).isEqualTo("qrs-refresh-token");
    }

    @Test
    void findByRefreshToken_shouldReturnEmpty_whenTokenDoesNotExist() {
        Optional<User> result = userRepository.findByRefreshToken("unknown-token");

        assertThat(result).isEmpty();
    }
}
