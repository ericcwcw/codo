package com.demo.codo.service;

import com.demo.codo.TestContainerConfig;
import com.demo.codo.entity.User;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.util.HashUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailVerificationIntegrationTest {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private EmailSender emailSender;

    private User testUser;

    @BeforeAll
    void setUpOnce() {
        userRepository.deleteAll();
        
        testUser = User.builder()
                .name("Test User")
                .email("test@test.com")
                .password("password123")
                .emailVerified(false)
                .build();
        testUser = userRepository.save(testUser);
    }
    
    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        testUser.setEmailVerified(false);
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldCallEmailSenderWhenSendingVerificationEmail() {
        emailVerificationService.sendVerificationEmail(testUser);

        verify(emailSender, times(1)).sendVerificationEmail(
                eq(testUser.getEmail()),
                anyString()
        );
    }

    @Test
    void shouldNotSendEmailIfAlreadyVerified() {
        testUser.setEmailVerified(true);
        userRepository.save(testUser);

        emailVerificationService.sendVerificationEmail(testUser);

        verify(emailSender, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void shouldVerifyUserWithValidToken() {
        String token = tokenService.generate(testUser.getId());

        boolean result = emailVerificationService.verifyEmail(token);
        assertTrue(result);
        
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(updatedUser.getEmailVerified());
    }

    @Test
    void shouldReturnFalseWithInvalidToken() {
        String invalidToken = "invalid-token";
        boolean result = emailVerificationService.verifyEmail(invalidToken);
        assertFalse(result);
        
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertFalse(updatedUser.getEmailVerified());
    }

    @Test
    void shouldReturnFalseWithExpiredToken() {
        String token = tokenService.generate(testUser.getId());
        String hashedToken = HashUtil.sha256Hex(token);
        redisTemplate.delete("email_verification:" + hashedToken);

        boolean result = emailVerificationService.verifyEmail(token);
        assertFalse(result);

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertFalse(updatedUser.getEmailVerified());
    }

    @Test
    void shouldReturnTrueWithAlreadyVerifiedUser() {
        String token = tokenService.generate(testUser.getId());
        testUser.setEmailVerified(true);
        userRepository.save(testUser);

        boolean result = emailVerificationService.verifyEmail(token);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWithNonExistentUser() {
        UUID nonExistentUserId = UUID.randomUUID();
        String token = tokenService.generate(nonExistentUserId);

        boolean result = emailVerificationService.verifyEmail(token);
        assertFalse(result);
    }

    @Test
    void shouldGenerateUniqueTokens() {
        String token1 = tokenService.generate(testUser.getId());
        String token2 = tokenService.generate(testUser.getId());

        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).hasSize(86);
        assertThat(token2).hasSize(86);
    }

    @Test
    void shouldStoreTokenInRedis() {
        String token = tokenService.generate(testUser.getId());

        String hashedToken = HashUtil.sha256Hex(token);
        String redisKey = "email_verification:" + hashedToken;
        String storedUserId = redisTemplate.opsForValue().get(redisKey);
        
        assertThat(storedUserId).isEqualTo(testUser.getId().toString());
    }

    @Test
    void shouldDeleteTokenAfterUse() {
        String token = tokenService.generate(testUser.getId());

        UUID userId = tokenService.verifyToken(token);
        assertThat(userId).isEqualTo(testUser.getId());
        
        UUID secondVerification = tokenService.verifyToken(token);
        assertThat(secondVerification).isNull();
    }
}
