package com.example.portfolio.auth.application;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

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
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@portfolio.dev");
        lenient().when(mailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(jakarta.mail.Session.getInstance(new Properties())));
    }

    // ── sendVerificationEmail — mail disabled ─────────────────────────────────

    @Test
    void sendVerificationEmail_shouldNotSendEmail_whenMailIsDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        emailService.sendVerificationEmail("user@test.com", "abc-token");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendVerificationEmail_shouldNotThrow_whenMailIsDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        assertThatCode(() -> emailService.sendVerificationEmail("user@test.com", "abc-token"))
                .doesNotThrowAnyException();
    }

    // ── sendVerificationEmail — mail enabled ──────────────────────────────────

    @Test
    void sendVerificationEmail_shouldCallMailSenderWithCorrectDetails_whenMailIsEnabled() throws Exception {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);

        emailService.sendVerificationEmail("user@test.com", "abc-token");

        ArgumentCaptor<MimeMessage> msgCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(msgCaptor.capture());
        MimeMessage sent = msgCaptor.getValue();

        assertThat(sent.getAllRecipients()).extracting(Object::toString).containsExactly("user@test.com");
        assertThat(sent.getFrom()).extracting(Object::toString).containsExactly("noreply@portfolio.dev");
        assertThat(sent.getSubject()).isEqualTo("Verify your email");

        String plainText = extractPlainText(sent);
        assertThat(plainText).contains("http://localhost:4200/confirm-email?token=abc-token");

        String html = extractHtml(sent);
        assertThat(html).contains("http://localhost:4200/confirm-email?token=abc-token");
        assertThat(html).contains("user@test.com");
        assertThat(html).contains("https://res.cloudinary.com/");
    }

    @Test
    void sendVerificationEmail_shouldNotPropagateException_whenMailSenderThrows() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        doThrow(new RuntimeException("SMTP connection refused")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> emailService.sendVerificationEmail("user@test.com", "abc-token"))
                .doesNotThrowAnyException();
    }

    // ── sendPasswordResetEmail — mail disabled ────────────────────────────────

    @Test
    void sendPasswordResetEmail_shouldNotSendEmail_whenMailIsDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        emailService.sendPasswordResetEmail("user@test.com", "xyz-token");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ── sendPasswordResetEmail — mail enabled ─────────────────────────────────

    @Test
    void sendPasswordResetEmail_shouldCallMailSenderWithCorrectDetails_whenMailIsEnabled() throws Exception {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);

        emailService.sendPasswordResetEmail("user@test.com", "xyz-token");

        ArgumentCaptor<MimeMessage> msgCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(msgCaptor.capture());
        MimeMessage sent = msgCaptor.getValue();

        assertThat(sent.getAllRecipients()).extracting(Object::toString).containsExactly("user@test.com");
        assertThat(sent.getFrom()).extracting(Object::toString).containsExactly("noreply@portfolio.dev");
        assertThat(sent.getSubject()).isEqualTo("Reset your password");

        String plainText = extractPlainText(sent);
        assertThat(plainText).contains("http://localhost:4200/reset-password?token=xyz-token");

        String html = extractHtml(sent);
        assertThat(html).contains("http://localhost:4200/reset-password?token=xyz-token");
    }

    @Test
    void sendPasswordResetEmail_shouldNotPropagateException_whenMailSenderThrows() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        doThrow(new RuntimeException("SMTP unavailable")).when(mailSender).send(any(MimeMessage.class));

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

    // ── helpers ────────────────────────────────────────────────────────────────

    private static String extractPlainText(MimeMessage message) throws Exception {
        message.saveChanges();
        return extractByMimeType(message.getContent(), "text/plain");
    }

    private static String extractHtml(MimeMessage message) throws Exception {
        message.saveChanges();
        return extractByMimeType(message.getContent(), "text/html");
    }

    private static String extractByMimeType(Object content, String mimeType) throws Exception {
        if (!(content instanceof Multipart multipart)) {
            return null;
        }
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            Object partContent = part.getContent();
            if (part.isMimeType(mimeType) && partContent instanceof String s) {
                return s;
            }
            if (partContent instanceof Multipart nestedMultipart) {
                String result = extractByMimeType(nestedMultipart, mimeType);
                if (result != null) return result;
            }
        }
        return null;
    }
}
