package com.demo.codo.service;

import com.demo.codo.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private static final String TOKEN_PREFIX = "email_verification:";
    private static final Duration TOKEN_EXPIRY = Duration.ofMinutes(10);
    private static final int TOKEN_LENGTH = 64;
    
    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(UUID userId) {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String hashedToken = HashUtil.sha256Hex(token);
        String redisKey = TOKEN_PREFIX + hashedToken;
        redisTemplate.opsForValue().set(redisKey, userId.toString(), TOKEN_EXPIRY);
        log.info("Generated verification token for user: {}, token: {}, hashedToken: {}, redisKey: {}", userId, token, hashedToken, redisKey);
        return token;
    }

    public UUID verifyToken(String token) {
        String hashedToken = HashUtil.sha256Hex(token);
        String redisKey = TOKEN_PREFIX + hashedToken;
        log.info("Verifying token: {}, hashedToken: {}, redisKey: {}", token, hashedToken, redisKey);

        String userIdStr = redisTemplate.opsForValue().get(redisKey);
        if (userIdStr != null) {
            redisTemplate.delete(redisKey);
            log.info("Token verified and deleted for user: {}", userIdStr);
            return UUID.fromString(userIdStr);
        }
        log.warn("Token verification failed - token not found or expired. Expected key: {}", redisKey);
        return null;
    }
}
