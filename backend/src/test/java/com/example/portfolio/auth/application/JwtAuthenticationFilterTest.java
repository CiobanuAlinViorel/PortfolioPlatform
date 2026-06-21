package com.example.portfolio.auth.application;

import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();

        testUser = User.builder()
                .email("user@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── no Authorization header ───────────────────────────────────────────────

    @Test
    void doFilterInternal_shouldPassThrough_whenNoAuthorizationHeader() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenAuthorizationHeaderIsNotBearer() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ── valid Bearer token ────────────────────────────────────────────────────

    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractEmail("valid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(testUser);
        when(jwtService.isTokenValid("valid-token", testUser)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@test.com");

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractEmail("valid-token");
        verify(userDetailsService).loadUserByUsername("user@test.com");
        verify(jwtService).isTokenValid("valid-token", testUser);
    }

    // ── invalid / expired token ───────────────────────────────────────────────

    @Test
    void doFilterInternal_shouldNotSetAuthentication_whenTokenFailsValidation() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtService.extractEmail("invalid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(testUser);
        when(jwtService.isTokenValid("invalid-token", testUser)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenJwtServiceThrowsDuringEmailExtraction() throws Exception {
        request.addHeader("Authorization", "Bearer malformed-token");

        when(jwtService.extractEmail("malformed-token")).thenThrow(new RuntimeException("Invalid JWT"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_shouldPassThrough_whenEmailExtractedIsNull() throws Exception {
        request.addHeader("Authorization", "Bearer token-with-null-subject");

        when(jwtService.extractEmail("token-with-null-subject")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    // ── already authenticated ─────────────────────────────────────────────────

    @Test
    void doFilterInternal_shouldSkipAuthentication_whenContextAlreadyHasAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@test.com", null, List.of())
        );
        request.addHeader("Authorization", "Bearer some-token");

        when(jwtService.extractEmail("some-token")).thenReturn("user@test.com");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractEmail("some-token");
        verifyNoInteractions(userDetailsService);
    }
}
