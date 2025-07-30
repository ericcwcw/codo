package com.demo.codo.controller;

import com.demo.codo.TestContainerConfig;
import com.demo.codo.constant.TestUser;
import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.UserRequest;
import com.demo.codo.entity.User;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@AutoConfigureWebMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithUserDetails(value = TestUser.EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
class TodoListControllerTest {
    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper;

    @BeforeAll
    void setUpOnce() {
        userRepository.deleteAll();

        UserRequest newUserRequest = UserRequest.builder()
                .name(TestUser.NAME)
                .email(TestUser.EMAIL)
                .password(TestUser.PASSWORD)
                .build();

        userService.create(newUserRequest);
    }


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldCreateTodoListSuccessfully() throws Exception {
        var request = new TodoListRequest("My Todo List", "A list for important tasks");

        mockMvc.perform(post("/api/v1/todo/lists")
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
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
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void shouldGetSingleTodoListById() throws Exception {
        var createRequest = new TodoListRequest("Test List", "Test description");
        
        String response = mockMvc.perform(post("/api/v1/todo/lists")
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        var jsonNode = objectMapper.readTree(response);
        String listId = jsonNode.get("id").asText();
        
        mockMvc.perform(get("/api/v1/todo/lists/{id}", listId)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listId))
                .andExpect(jsonPath("$.name").value("Test List"))
                .andExpect(jsonPath("$.description").value("Test description"));
    }

    @Test
    void shouldUpdateTodoListSuccessfully() throws Exception {
        var createRequest = new TodoListRequest("Original List", "Original description");
        
        String response = mockMvc.perform(post("/api/v1/todo/lists")
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        var jsonNode = objectMapper.readTree(response);
        String listId = jsonNode.get("id").asText();
        
        var updateRequest = new TodoListRequest("Updated List", "Updated description");

        mockMvc.perform(patch("/api/v1/todo/lists/{id}", listId)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listId))
                .andExpect(jsonPath("$.name").value("Updated List"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void shouldDeleteTodoListSuccessfully() throws Exception {
        var createRequest = new TodoListRequest("List to Delete", "This list will be deleted");
        
        String response = mockMvc.perform(post("/api/v1/todo/lists")
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        var jsonNode = objectMapper.readTree(response);
        String listId = jsonNode.get("id").asText();
        
        mockMvc.perform(delete("/api/v1/todo/lists/{id}", listId)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isNoContent());
        
        mockMvc.perform(get("/api/v1/todo/lists/{id}", listId)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isForbidden());
    }
}
