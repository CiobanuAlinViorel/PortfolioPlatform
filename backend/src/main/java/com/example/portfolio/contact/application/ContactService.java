package com.example.portfolio.contact.application;

import com.example.portfolio.auth.application.EmailService;
import com.example.portfolio.contact.dto.ContactMessageRequest;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ProfileRepository profileRepository;
    private final ContactRateLimiter rateLimiter;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public void sendMessage(ContactMessageRequest request, String clientIp) {
        rateLimiter.checkAndRecord(clientIp);

        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
        if (profile.getContactInfo() == null || profile.getContactInfo().getEmail() == null) {
            throw new NoSuchElementException("Owner contact email not configured");
        }

        emailService.sendContactMessage(
                profile.getContactInfo().getEmail(),
                request.getName(),
                request.getEmail(),
                request.getMessage()
        );
    }
}
