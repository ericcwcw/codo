package com.demo.codo.controller;

import com.demo.codo.TestContainerConfig;
import com.demo.codo.dto.UserRequest;
import com.demo.codo.entity.User;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.service.EmailSender;
import com.demo.codo.service.TokenService;
import com.demo.codo.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
class UserControllerEmailVerificationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldSendVerificationEmailWhenCreatingUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(emailSender, times(1)).sendVerificationEmail(
                eq("newuser@example.com"),
                anyString()
        );

        User createdUser = userRepository.findByEmail("newuser@example.com").orElseThrow();
        assertThat(createdUser.getEmailVerified()).isFalse();
    }

    @Test
    void shouldReturnSuccessWithValidToken() throws Exception {
        User unverifiedUser = User.builder()
                .name("Unverified User")
                .email("unverified@example.com")
                .password("password123")
                .emailVerified(false)
                .build();
        unverifiedUser = userRepository.save(unverifiedUser);

        String token = tokenService.generate(unverifiedUser.getId());

        mockMvc.perform(get("/api/v1/users/verify")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Email verified successfully\"}"));

        User verifiedUser = userRepository.findById(unverifiedUser.getId()).orElseThrow();
        assertThat(verifiedUser.getEmailVerified()).isTrue();
    }

    @Test
    void shouldReturnBadRequestWithInvalidToken() throws Exception {
        String invalidToken = "invalid-token";
        mockMvc.perform(get("/api/v1/users/verify")
                        .param("token", invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"message\": \"Invalid or expired verification token\"}"));
    }

    @Test
    void shouldReturnBadRequestWithExpiredToken() throws Exception {
        User unverifiedUser = User.builder()
                .name("Unverified User")
                .email("unverified@example.com")
                .password("password123")
                .emailVerified(false)
                .build();
        unverifiedUser = userRepository.save(unverifiedUser);

        String token = tokenService.generate(unverifiedUser.getId());
        String hashedToken = HashUtil.sha256Hex(token);
        redisTemplate.delete("email_verification:" + hashedToken);

        mockMvc.perform(get("/api/v1/users/verify")
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"message\": \"Invalid or expired verification token\"}"));

        User stillUnverifiedUser = userRepository.findById(unverifiedUser.getId()).orElseThrow();
        assertThat(stillUnverifiedUser.getEmailVerified()).isFalse();
    }

    @Test
    void shouldReturnSuccessWithAlreadyVerifiedUser() throws Exception {
        User verifiedUser = User.builder()
                .name("Verified User")
                .email("verified@example.com")
                .password("password123")
                .emailVerified(true)
                .build();
        verifiedUser = userRepository.save(verifiedUser);

        String token = tokenService.generate(verifiedUser.getId());

        mockMvc.perform(get("/api/v1/users/verify")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Email verified successfully\"}"));
    }

    @Test
    void shouldReturnBadRequestForPublicEndpointWithInvalidToken() throws Exception {
        String invalidToken = "invalid-token";

        mockMvc.perform(get("/api/v1/users/verify")
                        .param("token", invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
    }

    @Test
    void shouldReturnBadRequestWithMissingToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/verify"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldStillCreateUserWhenEmailSendingFails() throws Exception {
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailSender).sendVerificationEmail(anyString(), anyString());

        UserRequest request = UserRequest.builder()
                .name("New User")
                .email("newuser2@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        User createdUser = userRepository.findByEmail("newuser2@example.com").orElseThrow();
        assertThat(createdUser.getEmailVerified()).isFalse();
    }

    @Test
    void shouldNotSendEmailForDuplicateUser() throws Exception {
        User existingUser = User.builder()
                .name("Existing User")
                .email("existing@example.com")
                .password("password123")
                .emailVerified(true)
                .build();
        userRepository.save(existingUser);

        UserRequest request = UserRequest.builder()
                .name("Duplicate User")
                .email("existing@example.com")
                .password("newpassword")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email existing@example.com already exists"));

        verify(emailSender, never()).sendVerificationEmail(anyString(), anyString());
    }
}
