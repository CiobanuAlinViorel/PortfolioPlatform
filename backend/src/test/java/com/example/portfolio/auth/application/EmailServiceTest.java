package com.example.portfolio.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080/api");
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@portfolio.dev");
    }

    // ── sendVerificationEmail — mail disabled ─────────────────────────────────

    @Test
    void sendVerificationEmail_shouldNotSendEmail_whenMailIsDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        emailService.sendVerificationEmail("user@test.com", "abc-token");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendVerificationEmail_shouldNotThrow_whenMailIsDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        assertThatCode(() -> emailService.sendVerificationEmail("user@test.com", "abc-token"))
                .doesNotThrowAnyException();
    }

    // ── sendVerificationEmail — mail enabled ──────────────────────────────────

    @Test
    void sendVerificationEmail_shouldCallMailSenderWithCorrectDetails_whenMailIsEnabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);

        emailService.sendVerificationEmail("user@test.com", "abc-token");

        ArgumentCaptor<SimpleMailMessage> msgCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(msgCaptor.capture());
        SimpleMailMessage sent = msgCaptor.getValue();

        assertThat(sent.getTo()).containsExactly("user@test.com");
        assertThat(sent.getFrom()).isEqualTo("noreply@portfolio.dev");
        assertThat(sent.getSubject()).isEqualTo("Verify your email");
        assertThat(sent.getText()).contains("abc-token");
        assertThat(sent.getText()).contains("http://localhost:8080/api/auth/verify-email?token=abc-token");
    }

    @Test
    void sendVerificationEmail_shouldNotPropagateException_whenMailSenderThrows() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        doThrow(new RuntimeException("SMTP connection refused")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendVerificationEmail("user@test.com", "abc-token"))
                .doesNotThrowAnyException();
    }

    // ── sendPasswordResetEmail — mail disabled ────────────────────────────────

    @Test
    void sendPasswordResetEmail_shouldNotSendEmail_whenMailIsDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        emailService.sendPasswordResetEmail("user@test.com", "xyz-token");

        verifyNoInteractions(mailSender);
    }

    // ── sendPasswordResetEmail — mail enabled ─────────────────────────────────

    @Test
    void sendPasswordResetEmail_shouldCallMailSenderWithCorrectDetails_whenMailIsEnabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);

        emailService.sendPasswordResetEmail("user@test.com", "xyz-token");

        ArgumentCaptor<SimpleMailMessage> msgCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(msgCaptor.capture());
        SimpleMailMessage sent = msgCaptor.getValue();

        assertThat(sent.getTo()).containsExactly("user@test.com");
        assertThat(sent.getFrom()).isEqualTo("noreply@portfolio.dev");
        assertThat(sent.getSubject()).isEqualTo("Reset your password");
        assertThat(sent.getText()).contains("xyz-token");
        assertThat(sent.getText()).contains("http://localhost:8080/api/auth/reset-password?token=xyz-token");
    }

    @Test
    void sendPasswordResetEmail_shouldNotPropagateException_whenMailSenderThrows() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        doThrow(new RuntimeException("SMTP unavailable")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendPasswordResetEmail("user@test.com", "xyz-token"))
                .doesNotThrowAnyException();
    }

    // ── mailSender is null (not configured) ───────────────────────────────────

    @Test
    void sendVerificationEmail_shouldNotThrow_whenMailSenderBeanIsNotPresent() {
        ReflectionTestUtils.setField(emailService, "mailSender", null);
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);

        assertThatCode(() -> emailService.sendVerificationEmail("user@test.com", "token"))
                .doesNotThrowAnyException();
    }
}
