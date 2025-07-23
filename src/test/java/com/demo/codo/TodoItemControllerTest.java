package com.demo.codo;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Disabled
class TodoItemControllerTest extends BaseIntegrationTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    private UUID testListId;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        testListId = UUID.randomUUID();
    }

    @Test
    void shouldCreateTodoItemSuccessfully() throws Exception {
        TodoItemRequest request = new TodoItemRequest(
            "Complete project", 
            "Finish the Spring Boot project", 
            LocalDate.now().plusDays(7), 
            "PENDING", 
            testListId
        );

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Complete project"))
                .andExpect(jsonPath("$.description").value("Finish the Spring Boot project"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldGetTodoItemById() throws Exception {
        // Create a todo item first
        TodoItemRequest request = new TodoItemRequest("Test task", "Description", LocalDate.now(), "PENDING", testListId);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
                TodoItemDto.class
        );

        // Get the todo item
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test task"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldUpdateTodoItemSuccessfully() throws Exception {
        // Create a todo item first
        TodoItemRequest createRequest = new TodoItemRequest("Original task", "Original description", LocalDate.now(), "PENDING", testListId);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
                TodoItemDto.class
        );

        // Update the todo item
        TodoItemRequest updateRequest = new TodoItemRequest("Updated task", "Updated description", LocalDate.now().plusDays(1), "COMPLETED", testListId);
        mockMvc.perform(put("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldDeleteTodoItemSuccessfully() throws Exception {
        // Create a todo item first
        TodoItemRequest createRequest = new TodoItemRequest("Task to delete", "Description", null, "PENDING", testListId);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
                TodoItemDto.class
        );

        // Delete the todo item
        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId()))
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFilterTodoItemsByStatus() throws Exception {
        // Create items with different statuses
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 1", null, null, "PENDING", testListId))));
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 2", null, null, "COMPLETED", testListId))));
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 3", null, null, "PENDING", testListId))));

        // Filter by PENDING status
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testListId)
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFilterTodoItemsByListId() throws Exception {
        UUID listId1 = UUID.randomUUID();
        UUID listId2 = UUID.randomUUID();

        // Create items for different lists
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 1", null, null, null, listId1))));
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 2", null, null, null, listId2))));
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 3", null, null, null, listId1))));

        // Filter by listId1
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", listId1)
                .param("listId", listId1.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldHandlePagination() throws Exception {
        // Create multiple todo items
        for (int i = 1; i <= 5; i++) {
            TodoItemRequest request = new TodoItemRequest("Task " + i, "Description " + i, null, "PENDING", testListId);
            mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        // Get first page
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testListId)
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldReturn404ForNonExistentTodoItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testListId, nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailToCreateTodoItemWithBlankName() throws Exception {
        TodoItemRequest request = new TodoItemRequest();
        request.setName("");
        request.setListId(testListId);

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToCreateTodoItemWithLongName() throws Exception {
        String longName = "a".repeat(256);
        TodoItemRequest request = new TodoItemRequest();
        request.setName(longName);
        request.setListId(testListId);

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToDeleteNonExistentTodoItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/items/{id}", testListId, nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleSpecialCharactersInSearch() throws Exception {
        // Create item with special characters
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task with @#$%", "Special chars: !@#$%^&*()", null, null, testListId))));

        // Search with special characters
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testListId)
                .param("search", "@#$%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldHandleUnicodeCharacters() throws Exception {
        String unicodeName = "タスク 🚀 测试";
        TodoItemRequest request = new TodoItemRequest(unicodeName, "Unicode description 中文", null, null, testListId);

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(unicodeName))
                .andExpect(jsonPath("$.description").value("Unicode description 中文"));
    }

    @Test
    void shouldHandleNullValuesGracefully() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Simple task", null, null, null, testListId);

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Simple task"))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.id").exists());
    }
}
