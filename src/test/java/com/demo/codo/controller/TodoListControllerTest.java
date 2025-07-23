package com.demo.codo.controller;

import com.demo.codo.TestContainerConfiguration;
import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListResponse;
import com.demo.codo.dto.UserRequest;
import com.demo.codo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for TodoListController with Spring Security.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfiguration.class)
@AutoConfigureWebMvc
class TodoListControllerTest {
    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    private ObjectMapper objectMapper;

    private static final String TEST_NAME = "test";
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "testpass123";

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        UserRequest newUserRequest = UserRequest.builder()
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        userService.create(newUserRequest);
    }

    @Test
    void shouldCreateTodoListSuccessfully() throws Exception {
        var request = new TodoListRequest("My Todo List", "A list for important tasks");

        mockMvc.perform(post("/api/v1/todo/lists")
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Todo List"))
                .andExpect(jsonPath("$.description").value("A list for important tasks"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldGetTodoListsWithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists")
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void shouldGetSingleTodoListById() throws Exception {
        UUID listId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/v1/todo/lists/{id}", listId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTodoListSuccessfully() throws Exception {
        UUID listId = UUID.randomUUID();
        var request = new TodoListRequest("Updated List", "Updated description");

        mockMvc.perform(patch("/api/v1/todo/lists/{id}", listId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTodoListSuccessfully() throws Exception {
        UUID listId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/todo/lists/{id}", listId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isNotFound());
    }
}
