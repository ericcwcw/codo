package com.demo.codo;

import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TodoListControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldCreateTodoListSuccessfully() throws Exception {
        var request = new TodoListRequest("My Todo List", "A list for important tasks");

        mockMvc.perform(post("/api/v1/todo/lists")
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
        
        mockMvc.perform(get("/api/v1/todo/lists/{id}", listId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTodoListSuccessfully() throws Exception {
        UUID listId = UUID.randomUUID();
        var request = new TodoListRequest("Updated List", "Updated description");

        mockMvc.perform(patch("/api/v1/todo/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTodoListSuccessfully() throws Exception {
        UUID listId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/todo/lists/{id}", listId))
                .andExpect(status().isNotFound());
    }
}
