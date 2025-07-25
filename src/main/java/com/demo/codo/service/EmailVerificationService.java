package com.demo.codo.service;

import com.demo.codo.entity.User;
import com.demo.codo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailSender emailSender;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    
    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(User user) {
        if (user.getEmailVerified()) {
            log.debug("User {} already verified, skipping verification email", user.getEmail());
            return;
        }
        String token = tokenService.generate(user.getId());
        String verificationLink = String.format("%s/api/v1/users/verify?token=%s", baseUrl, token);
        emailSender.sendVerificationEmail(user.getEmail(), verificationLink);
        log.info("Verification email sent to user: {}", user.getEmail());
    }

    public boolean verifyEmail(String token) {
        UUID userId = tokenService.verifyToken(token);
        if (userId == null) {
            log.debug("Token verification failed - invalid or expired token");
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User not found, id={}", userId);
            return false;
        }
        if (user.getEmailVerified()) {
            log.debug("User {} already verified", user.getEmail());
            return true;
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified successfully for user: {}", user.getEmail());
        return true;
    }
}
