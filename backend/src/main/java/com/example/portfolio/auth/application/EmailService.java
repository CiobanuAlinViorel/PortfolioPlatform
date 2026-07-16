package com.example.portfolio.auth.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080/api}")
    private String baseUrl;

    @Value("${app.mail.from:alinviorelciobanu@gmail.com}")
    private String mailFrom;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public String sendVerificationEmail(String to, String token) {
        String link = baseUrl + "/auth/verify-email?token=" + token;
        String body = "Please verify your email by clicking the link below:\n\n" + link
                + "\n\nThis link expires in 24 hours.";
        send(to, "Verify your email", body);
        return link;
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = baseUrl + "/auth/reset-password?token=" + token;
        String body = "Click the link below to reset your password:\n\n" + link
                + "\n\nThis link expires in 1 hour. If you did not request a reset, ignore this email.";
        send(to, "Reset your password", body);
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled || mailSender == null) {
            log.info("[EMAIL SIMULATION] To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}. Logging content instead.", to, e.getMessage());
            log.info("[EMAIL FALLBACK] To: {} | Subject: {} | Body: {}", to, subject, body);
        }
    }
}
