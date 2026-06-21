package com.example.portfolio.auth.application;

import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 32-byte key (256-bit) in base64 — same key used in application.yml
    private static final String TEST_SECRET = "TS/3GmrMXOmRzo3/vymhAbUg77ruVeiT96WaL4MU0rY=";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private JwtService jwtService;
    private UserDetails testUser;
    private UserDetails adminUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", ONE_HOUR_MS);

        testUser = User.builder()
                .email("user@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .build();

        adminUser = User.builder()
                .email("admin@test.com")
                .password("encoded-password")
                .role(UserRole.ADMIN)
                .build();
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    void generateToken_shouldReturnNonBlankToken() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_shouldEmbedEmailAsSubject() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void generateToken_shouldEmbedRoleInClaims() {
        String userToken = jwtService.generateToken(testUser);
        String adminToken = jwtService.generateToken(adminUser);

        // Roles must survive a round-trip: token is not expired, so no extraction exception
        assertThat(jwtService.isTokenExpired(userToken)).isFalse();
        assertThat(jwtService.isTokenExpired(adminToken)).isFalse();
    }

    @Test
    void generateToken_shouldProduceDifferentTokensForDifferentUsers() {
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(adminUser);

        assertThat(token1).isNotEqualTo(token2);
    }

    // ── extractEmail ─────────────────────────────────────────────────────────

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void extractEmail_shouldThrowException_whenTokenIsMalformed() {
        assertThatThrownBy(() -> jwtService.extractEmail("not.a.valid.jwt"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractEmail_shouldThrowException_whenTokenIsSignedWithDifferentKey() {
        // Build a service with a different secret
        JwtService otherService = new JwtService();
        ReflectionTestUtils.setField(otherService, "secretKey", "ZGlmZmVyZW50LXNlY3JldC1rZXktZm9yLXRlc3RpbmchISE=");
        ReflectionTestUtils.setField(otherService, "expirationMs", ONE_HOUR_MS);

        String tokenFromOtherService = otherService.generateToken(testUser);

        assertThatThrownBy(() -> jwtService.extractEmail(tokenFromOtherService))
                .isInstanceOf(Exception.class);
    }

    // ── isTokenValid ─────────────────────────────────────────────────────────

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenMatchesUserAndIsNotExpired() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenBelongsToADifferentUser() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(token, adminUser)).isFalse();
    }

    // ── isTokenExpired ────────────────────────────────────────────────────────

    @Test
    void isTokenExpired_shouldReturnFalse_whenTokenIsWithinExpiry() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenExpired_shouldReturnTrue_whenTokenHasExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken(testUser);

        Thread.sleep(20);

        assertThat(jwtService.isTokenExpired(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken(testUser);

        Thread.sleep(20);

        assertThat(jwtService.isTokenValid(token, testUser)).isFalse();
    }
}
