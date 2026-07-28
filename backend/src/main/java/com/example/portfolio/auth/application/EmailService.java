package com.example.portfolio.auth.application;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailService {

    // Uploaded once to Cloudinary (public_id "email/logo") so it renders as a normal
    // hosted image in every email client. Gmail and others strip/block data: URI images,
    // and inline CID attachments show up as a distracting "1 attachment" in Gmail.
    private static final String LOGO_URL = "https://res.cloudinary.com/detjbuy4n/image/upload/v1784374141/email/logo.png";

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.mail.from:alinviorelciobanu@gmail.com}")
    private String mailFrom;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public String sendVerificationEmail(String to, String token) {
        String link = frontendUrl + "/confirm-email?token=" + token;
        String html = loadTemplate("email-templates/verification-email.html")
                .replace("{{LOGO}}", LOGO_URL)
                .replace("{{EMAIL}}", to)
                .replace("{{LINK}}", link);
        String plainText = "Please verify your email by clicking the link below:\n\n" + link
                + "\n\nThis link expires in 24 hours.";
        send(to, "Verify your email", plainText, html);
        return link;
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String html = loadTemplate("email-templates/reset-password-email.html")
                .replace("{{LOGO}}", LOGO_URL)
                .replace("{{EMAIL}}", to)
                .replace("{{LINK}}", link);
        String plainText = "Click the link below to reset your password:\n\n" + link
                + "\n\nThis link expires in 1 hour. If you did not request a reset, ignore this email.";
        send(to, "Reset your password", plainText, html);
    }

    public void sendContactMessage(String toOwner, String senderName, String senderEmail, String message) {
        String html = loadTemplate("email-templates/contact-message-email.html")
                .replace("{{LOGO}}", LOGO_URL)
                .replace("{{SENDER_NAME}}", escapeHtml(senderName))
                .replace("{{SENDER_EMAIL}}", escapeHtml(senderEmail))
                .replace("{{MESSAGE}}", escapeHtml(message).replace("\n", "<br>"));
        String plainText = "New message from " + senderName + " (" + senderEmail + "):\n\n" + message;
        send(toOwner, "New portfolio contact message from " + senderName, plainText, html, senderEmail);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void send(String to, String subject, String plainText, String html) {
        send(to, subject, plainText, html, null);
    }

    private void send(String to, String subject, String plainText, String html, String replyTo) {
        if (!mailEnabled || mailSender == null) {
            log.info("[EMAIL SIMULATION] To: {} | Subject: {} | Body: {}", to, subject, plainText);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}. Logging content instead.", to, e.getMessage());
            log.info("[EMAIL FALLBACK] To: {} | Subject: {} | Body: {}", to, subject, plainText);
        }
    }

    private String loadTemplate(String path) {
        try {
            return StreamUtils.copyToString(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load email template: " + path, e);
        }
    }
}
