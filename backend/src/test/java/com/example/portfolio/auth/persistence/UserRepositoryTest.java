package com.example.portfolio.auth.persistence;

import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists(){
        User user = User.builder()
                .role(UserRole.USER)
                .email("admin@test.ro")
                .password("encoded_password")
                .build();
        userRepository.save(user);

        Optional<User> result  = userRepository.findByEmail(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@test.ro");
        assertThat(result.get().getRole()).isEqualTo(UserRole.USER);
    }

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
}
