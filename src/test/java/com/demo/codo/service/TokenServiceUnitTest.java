package com.demo.codo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceUnitTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenService = new TokenService(redisTemplate);
    }

    @Test
    void shouldCreateUrlSafeToken() {
        UUID userId = UUID.randomUUID();
        String token = tokenService.generate(userId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        assertThat(token).doesNotContain("+");
        assertThat(token).doesNotContain("=");
        assertThat(token).doesNotContain("/");

        assertThat(token).matches("^[A-Za-z0-9_-]+$");

        verify(valueOperations).set(anyString(), eq(userId.toString()), any(Duration.class));
    }

    @Test
    void shouldReturnUserIdWithValidToken() {
        UUID userId = UUID.randomUUID();
        String token = tokenService.generate(userId);

        when(valueOperations.get(anyString())).thenReturn(userId.toString());
        when(redisTemplate.delete(anyString())).thenReturn(true);

        UUID result = tokenService.verifyToken(token);

        assertThat(result).isEqualTo(userId);
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void shouldReturnNullWithInvalidToken() {
        String invalidToken = "invalid-token";
        when(valueOperations.get(anyString())).thenReturn(null);
        UUID result = tokenService.verifyToken(invalidToken);
        assertThat(result).isNull();
    }

    @Test
    void shouldCreateUniqueTokens() {
        UUID userId = UUID.randomUUID();
        String token1 = tokenService.generate(userId);
        String token2 = tokenService.generate(userId);
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).matches("^[A-Za-z0-9_-]+$");
        assertThat(token2).matches("^[A-Za-z0-9_-]+$");
    }
}
