package com.demo.codo.controller;

import com.demo.codo.TestContainerConfig;
import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemRequest;
import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.UserRequest;
import com.demo.codo.entity.TodoList;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import com.demo.codo.enums.TodoItemStatus;
import com.demo.codo.repository.TodoListRepository;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.repository.UserTodoListRepository;
import com.demo.codo.service.TodoListService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
@AutoConfigureWebMvc
class TodoItemControllerTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TodoListService todoListService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserTodoListRepository userTodoListRepository;

    private ObjectMapper objectMapper;

    private UUID testListId;

    private static final String TEST_NAME = "test";
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "testpass123";

    @BeforeEach
    void setUp() {
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
        try {
            userService.create(newUserRequest);
        } catch (com.demo.codo.exception.DuplicateUserException e) {
            // User already exists, which is fine for test setup
        }

        TodoListRequest listRequest = TodoListRequest.builder()
                .name("Test List")
                .description("Test list for integration tests")
                .build();
        TodoListDto createdList = todoListService.create(listRequest);
        testListId = createdList.getId();

        User testUser = userRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test user not found"));
        
        UserTodoList ownerRecord = UserTodoList.builder()
                .userId(testUser.getId())
                .listId(testListId)
                .isOwner(true)
                .isEditable(true)
                .build();
        
        userTodoListRepository.save(ownerRecord);
    }

    private TodoListDto createTodoListWithOwner(String name, String description) {
        TodoListRequest request = new TodoListRequest(name, description);
        TodoListDto createdList = todoListService.create(request);
        
        User testUser = userRepository.findByEmail(TEST_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test user not found"));
        
        UserTodoList ownerRecord = UserTodoList.builder()
                .userId(testUser.getId())
                .listId(createdList.getId())
                .isOwner(true)
                .isEditable(true)
                .build();
        
        userTodoListRepository.save(ownerRecord);
        return createdList;
    }

    @Test
    void shouldCreateTodoItemSuccessfully() throws Exception {
        TodoItemRequest request = new TodoItemRequest(
            "Complete project", 
            "Finish the Spring Boot project", 
            LocalDate.now().plusDays(7), 
            TodoItemStatus.TODO, 
            null
        );

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Complete project"))
                .andExpect(jsonPath("$.description").value("Finish the Spring Boot project"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldGetTodoItemById() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Test task", "Description", LocalDate.now(), TodoItemStatus.TODO, null);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
                TodoItemDto.class
        );

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId())
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test task"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void shouldUpdateTodoItemSuccessfully() throws Exception {
        TodoItemRequest createRequest = new TodoItemRequest("Original task", "Original text", LocalDate.now(), TodoItemStatus.TODO, null);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), TodoItemDto.class);

        TodoItemRequest updateRequest = new TodoItemRequest("Updated task", "Updated text", LocalDate.now().plusDays(1), TodoItemStatus.COMPLETED, null);
        mockMvc.perform(put("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId())
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated task"))
                .andExpect(jsonPath("$.description").value("Updated text"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldDeleteTodoItemSuccessfully() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Task to delete", "Description", LocalDate.now(), TodoItemStatus.TODO, null);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), TodoItemDto.class);

        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId())
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testListId, created.getId())
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFilterTodoItemsByStatus() throws Exception {
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.TODO, null))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 2", "Description 2", LocalDate.now(), TodoItemStatus.COMPLETED, null))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 3", "Description 3", LocalDate.now(), TodoItemStatus.TODO, null))));

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFilterTodoItemsByListId() throws Exception {
        TodoListDto list1 = createTodoListWithOwner("Test List 1", "First test list");
        TodoListDto list2 = createTodoListWithOwner("Test List 2", "Second test list");
        
        UUID listId1 = list1.getId();
        UUID listId2 = list2.getId();

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId1)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.TODO, null))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId2)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 2", "Description 2", LocalDate.now(), TodoItemStatus.TODO, null))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId1)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 3", "Description 3", LocalDate.now(), TodoItemStatus.TODO, null))));

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", listId1)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldHandlePagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                    .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new TodoItemRequest("Task " + i, "Description " + i, LocalDate.now(), TodoItemStatus.TODO, null))));
        }

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void shouldReturn404ForNonExistentTodoItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testListId, nonExistentId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailToCreateTodoItemWithBlankName() throws Exception {
        TodoItemRequest request = new TodoItemRequest("", "Description", LocalDate.now(), TodoItemStatus.TODO, null);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToCreateTodoItemWithLongName() throws Exception {
        String longName = "a".repeat(256);
        TodoItemRequest request = new TodoItemRequest(longName, "Description", LocalDate.now(), TodoItemStatus.TODO, null);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToDeleteNonExistentTodoItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/items/{id}", testListId, nonExistentId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleSpecialCharactersInSearch() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Special chars: @#$%^&*()", "Description with émojis 🎉", LocalDate.now(), TodoItemStatus.TODO, null);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .param("search", "Special"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleUnicodeCharacters() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Unicode: 你好世界", "Description: こんにちは", LocalDate.now(), TodoItemStatus.TODO, null);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Unicode: 你好世界"));
    }

    @Test
    void shouldHandleNullValuesGracefully() throws Exception {
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testListId)
                .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
