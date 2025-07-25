package com.demo.codo.security;

import com.demo.codo.TestContainerConfig;
import com.demo.codo.dto.UserRequest;
import com.demo.codo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@AutoConfigureWebMvc
class AuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    private static final String TEST_NAME = "test";
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "testpass123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        UserRequest newUserRequest = UserRequest.builder()
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        try {
            userService.create(newUserRequest);
        } catch (com.demo.codo.exception.DuplicateUserException e) {
            // User already exists, which is fine for test setup
        }
    }

    @Test
    void shouldAllowAccessWithValidCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists")
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithInvalidCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists")
                .with(httpBasic(TEST_EMAIL, "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists")
                .with(httpBasic("nonexistent", TEST_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldProtectAllTodoEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/todo/lists/123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(status().isUnauthorized());
    }
}
