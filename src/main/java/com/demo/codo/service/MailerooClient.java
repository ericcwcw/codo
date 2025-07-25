package com.demo.codo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@ConditionalOnProperty(name = "maileroo.mock.enabled", havingValue = "false", matchIfMissing = true)
public class MailerooClient implements EmailSender {

    private final RestTemplate restTemplate;
    private final String fromEmail;
    private final String apiToken;
    private static final String MAILEROO_API_URL = "https://smtp.maileroo.com/send";

    public MailerooClient(@Value("${maileroo.api.token}") String apiToken,
                         @Value("${maileroo.from.email}") String fromEmail) {
        this.apiToken = apiToken;
        this.fromEmail = fromEmail;
        this.restTemplate = new RestTemplate();
        log.info("MailerooClient initialized with from email: {}", fromEmail);
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        try {
            String htmlContent = buildVerificationEmailHtml(verificationLink);
            String textContent = buildVerificationEmailText(verificationLink);
            
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("from", "Email Verification <" + fromEmail + ">");
            formData.add("to", toEmail);
            formData.add("subject", "Verify Your Email Address");
            formData.add("html", htmlContent);
            formData.add("plain", textContent);
            
            // Set headers as per Maileroo API documentation
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-API-Key", apiToken);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            
            log.info("Sending verification email to: {} via Maileroo API", toEmail);
            
            // Send email via Maileroo API using RestTemplate
            ResponseEntity<String> response = restTemplate.postForEntity(MAILEROO_API_URL, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Verification email sent successfully to: {} - Response: {}", toEmail, response.getBody());
            } else {
                log.error("Failed to send verification email to: {} - Status: {} - Response: {}", 
                         toEmail, response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send verification email - Status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Failed to send verification email to: {} via Maileroo - Error: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email via Maileroo: " + e.getMessage(), e);
        }
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
