package com.demo.codo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "maileroo.mock.enabled", havingValue = "true", matchIfMissing = false)
public class MockEmailSender implements EmailSender {

    private final String fromEmail;

    public MockEmailSender(@Value("${maileroo.from.email}") String fromEmail) {
        this.fromEmail = fromEmail;
        log.info("MockEmailSender initialized - emails will be logged instead of sent");
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        String htmlContent = buildVerificationEmailHtml(verificationLink);
        String textContent = buildVerificationEmailText(verificationLink);

        log.info("=== EMAIL VERIFICATION (MOCK MODE) ===");
        log.info("To: {}", toEmail);
        log.info("From: {}", fromEmail);
        log.info("Subject: Verify Your Email Address");
        log.info("Verification Link: {}", verificationLink);
        log.info("HTML Content: {}", htmlContent);
        log.info("Text Content: {}", textContent);
        log.info("=== END EMAIL ===");

        log.info("Mock verification email sent successfully to: {}", toEmail);
    }

    private String buildVerificationEmailHtml(String verificationLink) {
        return String.format("""
            <html>
            <body>
                <h2>Email Verification</h2>
                <p>Please click the link below to verify your email address:</p>
                <p><a href="%s">Verify Email</a></p>
                <p>Or copy and paste this link in your browser:</p>
                <p>%s</p>
                <p>This link will expire in 1 minute.</p>
            </body>
            </html>
            """, verificationLink, verificationLink);
    }

    private String buildVerificationEmailText(String verificationLink) {
        return String.format("""
            Email Verification
            
            Please copy and paste the following link in your browser to verify your email address:
            
            %s
            
            This link will expire in 1 minute.
            """, verificationLink);
    }
}
