package com.example.portfolio.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@portfolio.dev");
        ReflectionTestUtils.setField(emailService, "resendApiKey", "test-api-key");
    }

    private void stubRestClientChain() {
        lenient().when(restClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    // ── sendPasswordResetEmail — mail disabled ────────────────────────────────

    @Test
    void sendPasswordResetEmail_shouldNotSendEmail_whenMailIsDisabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        emailService.sendPasswordResetEmail("user@test.com", "xyz-token");

        verifyNoInteractions(restClient);
    }

    // ── sendPasswordResetEmail — mail enabled ─────────────────────────────────

    @Test
    void sendPasswordResetEmail_shouldCallResendApiWithCorrectDetails_whenMailIsEnabled() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        stubRestClientChain();

        emailService.sendPasswordResetEmail("user@test.com", "xyz-token");

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestBodySpec).body(payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload.get("to")).isEqualTo("user@test.com");
        assertThat(payload.get("from")).isEqualTo("noreply@portfolio.dev");
        assertThat(payload.get("subject")).isEqualTo("Reset your password");
        assertThat((String) payload.get("text")).contains("http://localhost:4200/reset-password?token=xyz-token");
        assertThat((String) payload.get("html")).contains("http://localhost:4200/reset-password?token=xyz-token");
    }

    @Test
    void sendPasswordResetEmail_shouldNotPropagateException_whenRestClientThrows() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        when(restClient.post()).thenThrow(new RuntimeException("Connection refused"));

        assertThatCode(() -> emailService.sendPasswordResetEmail("user@test.com", "xyz-token"))
                .doesNotThrowAnyException();
    }

    // ── resendApiKey is blank (not configured) ────────────────────────────────

    @Test
    void sendPasswordResetEmail_shouldNotThrow_whenApiKeyIsBlank() {
        ReflectionTestUtils.setField(emailService, "resendApiKey", "");
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);

        assertThatCode(() -> emailService.sendPasswordResetEmail("user@test.com", "token"))
                .doesNotThrowAnyException();

        verifyNoInteractions(restClient);
    }

    // ── sendContactMessage — reply-to ─────────────────────────────────────────

    @Test
    void sendContactMessage_shouldSetReplyToSenderEmail() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        stubRestClientChain();

        emailService.sendContactMessage("owner@test.com", "Jane", "jane@test.com", "Hello there");

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestBodySpec).body(payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload.get("to")).isEqualTo("owner@test.com");
        assertThat(payload.get("reply_to")).isEqualTo("jane@test.com");
        assertThat(payload.get("subject")).isEqualTo("New portfolio contact message from Jane");
    }
}
