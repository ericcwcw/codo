package com.demo.codo.controller;

import com.demo.codo.annotation.AuthenticatedIntegrationTest;
import com.demo.codo.constant.TestUser;
import com.demo.codo.dto.*;
import com.demo.codo.entity.User;
import com.demo.codo.enums.TodoItemStatus;
import com.demo.codo.repository.TodoListRepository;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.repository.UserTodoListRepository;
import com.demo.codo.service.TodoListService;
import com.demo.codo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AuthenticatedIntegrationTest
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
    private TodoListRepository todoListRepository;
    
    @Autowired
    private UserTodoListRepository userTodoListRepository;

    private ObjectMapper objectMapper;

    private TodoListDto testList;

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

        userTodoListRepository.deleteAll();
        todoListRepository.deleteAll();
        
        testList = createTodoList("Test List", "Test list for integration tests");
    }

    private TodoListDto createTodoList(String name, String description) {
        TodoListRequest request = new TodoListRequest(name, description);
        return todoListService.create(request);
    }

    @Test
    void shouldCreateTodoItemSuccessfully() throws Exception {
        TodoItemRequest request = new TodoItemRequest(
            "Complete project", 
            "Finish the Spring Boot project", 
            LocalDate.now().plusDays(7), 
            TodoItemStatus.TODO
        );

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
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
        TodoItemRequest request = new TodoItemRequest("Test task", "Description", LocalDate.now(), TodoItemStatus.TODO);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
                TodoItemDto.class
        );

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testList.getId(), created.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test task"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void shouldUpdateTodoItemSuccessfully() throws Exception {
        TodoItemRequest createRequest = new TodoItemRequest("Original task", "Original text", LocalDate.now(), TodoItemStatus.TODO);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), TodoItemDto.class);

        TodoItemRequest updateRequest = new TodoItemRequest("Updated task", "Updated text", LocalDate.now().plusDays(1), TodoItemStatus.COMPLETED);
        mockMvc.perform(patch("/api/v1/todo/lists/{listId}/items/{id}", testList.getId(), created.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated task"))
                .andExpect(jsonPath("$.description").value("Updated text"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldDeleteTodoItemSuccessfully() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Task to delete", "Description", LocalDate.now(), TodoItemStatus.TODO);
        MvcResult createResult = mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        TodoItemDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), TodoItemDto.class);

        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/items/{id}", testList.getId(), created.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testList.getId(), created.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFilterTodoItemsByStatus() throws Exception {
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.TODO))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 2", "Description 2", LocalDate.now(), TodoItemStatus.COMPLETED))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 3", "Description 3", LocalDate.now(), TodoItemStatus.TODO))));

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFilterTodoItemsByListId() throws Exception {
        TodoListDto list1 = createTodoList("Test List 1", "First test list");
        TodoListDto list2 = createTodoList("Test List 2", "Second test list");
        
        UUID listId1 = list1.getId();
        UUID listId2 = list2.getId();

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId1)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.TODO))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId2)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 2", "Description 2", LocalDate.now(), TodoItemStatus.TODO))));
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", listId1)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TodoItemRequest("Task 3", "Description 3", LocalDate.now(), TodoItemStatus.TODO))));

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", listId1)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldHandlePagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                    .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new TodoItemRequest("Task " + i, "Description " + i, LocalDate.now(), TodoItemStatus.TODO))));
        }

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void shouldReturn404ForNonExistentTodoItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items/{id}", testList.getId(), nonExistentId)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailToCreateTodoItemWithBlankName() throws Exception {
        TodoItemRequest request = new TodoItemRequest("", "Description", LocalDate.now(), TodoItemStatus.TODO);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToCreateTodoItemWithLongName() throws Exception {
        String longName = "a".repeat(256);
        TodoItemRequest request = new TodoItemRequest(longName, "Description", LocalDate.now(), TodoItemStatus.TODO);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToDeleteNonExistentTodoItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/items/{id}", testList.getId(), nonExistentId)
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleSpecialCharactersInSearch() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Special chars: @#$%^&*()", "Description with émojis 🎉", LocalDate.now(), TodoItemStatus.TODO);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("search", "Special"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleUnicodeCharacters() throws Exception {
        TodoItemRequest request = new TodoItemRequest("Unicode: 你好世界", "Description: こんにちは", LocalDate.now(), TodoItemStatus.TODO);
        
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Unicode: 你好世界"));
    }

    @Test
    void shouldHandleNullValuesGracefully() throws Exception {
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSortTodoItemsByStatusAscending() throws Exception {
        createTodoItem("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.COMPLETED);
        createTodoItem("Task 2", "Description 2", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Task 3", "Description 3", LocalDate.now(), TodoItemStatus.IN_PROGRESS);
        createTodoItem("Task 4", "Description 4", LocalDate.now(), TodoItemStatus.CANCELLED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", "status,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.content[1].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[2].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.content[3].status").value("TODO"));
    }

    @Test
    void shouldSortTodoItemsByStatusDescending() throws Exception {
        createTodoItem("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.COMPLETED);
        createTodoItem("Task 2", "Description 2", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Task 3", "Description 3", LocalDate.now(), TodoItemStatus.IN_PROGRESS);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", "status,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.content[1].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.content[2].status").value("COMPLETED"));
    }

    @Test
    void shouldSortTodoItemsByNameAscending() throws Exception {
        createTodoItem("Zebra Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Alpha Task", "Description 2", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Beta Task", "Description 3", LocalDate.now(), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alpha Task"))
                .andExpect(jsonPath("$.content[1].name").value("Beta Task"))
                .andExpect(jsonPath("$.content[2].name").value("Zebra Task"));
    }

    @Test
    void shouldSortTodoItemsByNameDescending() throws Exception {
        createTodoItem("Alpha Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Zebra Task", "Description 2", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Beta Task", "Description 3", LocalDate.now(), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Zebra Task"))
                .andExpect(jsonPath("$.content[1].name").value("Beta Task"))
                .andExpect(jsonPath("$.content[2].name").value("Alpha Task"));
    }

    @Test
    void shouldSortTodoItemsByDueDateAscending() throws Exception {
        createTodoItem("Task 1", "Description 1", LocalDate.now().plusDays(3), TodoItemStatus.TODO);
        createTodoItem("Task 2", "Description 2", LocalDate.now().plusDays(1), TodoItemStatus.TODO);
        createTodoItem("Task 3", "Description 3", LocalDate.now().plusDays(2), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", "dueDate,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Task 2"))
                .andExpect(jsonPath("$.content[1].name").value("Task 3"))
                .andExpect(jsonPath("$.content[2].name").value("Task 1"));
    }

    @Test
    void shouldSortTodoItemsByDueDateDescending() throws Exception {
        createTodoItem("Task 1", "Description 1", LocalDate.now().plusDays(1), TodoItemStatus.TODO);
        createTodoItem("Task 2", "Description 2", LocalDate.now().plusDays(3), TodoItemStatus.TODO);
        createTodoItem("Task 3", "Description 3", LocalDate.now().plusDays(2), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", "dueDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Task 2"))
                .andExpect(jsonPath("$.content[1].name").value("Task 3"))
                .andExpect(jsonPath("$.content[2].name").value("Task 1"));
    }


    @Test
    void shouldSortTodoItemsWithMultipleSortFields() throws Exception {
        createTodoItem("Zebra Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Alpha Task", "Description 2", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Beta Task", "Description 3", LocalDate.now(), TodoItemStatus.COMPLETED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", "status,asc")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Beta Task"))  // COMPLETED status first
                .andExpect(jsonPath("$.content[1].name").value("Alpha Task"))  // TODO status, alphabetically first
                .andExpect(jsonPath("$.content[2].name").value("Zebra Task")); // TODO status, alphabetically last
    }

    @Test
    void shouldHandleEmptySortParameter() throws Exception {
        createTodoItem("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("sort", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Task 1"));
    }

    @Test
    void shouldHandleNoSortParameter() throws Exception {
        createTodoItem("Task 1", "Description 1", LocalDate.now(), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Task 1"));
    }

    @Test
    void shouldFilterTodoItemsBySpecificStatus() throws Exception {
        createTodoItem("Todo Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Completed Task", "Description 2", LocalDate.now(), TodoItemStatus.COMPLETED);
        createTodoItem("In Progress Task", "Description 3", LocalDate.now(), TodoItemStatus.IN_PROGRESS);
        createTodoItem("Another Todo Task", "Description 4", LocalDate.now(), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.content[1].status").value("TODO"));
    }

    @Test
    void shouldFilterTodoItemsByCompletedStatus() throws Exception {
        createTodoItem("Todo Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Completed Task", "Description 2", LocalDate.now(), TodoItemStatus.COMPLETED);
        createTodoItem("Another Completed Task", "Description 3", LocalDate.now(), TodoItemStatus.COMPLETED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[1].status").value("COMPLETED"));
    }

    @Test
    void shouldFilterTodoItemsByInProgressStatus() throws Exception {
        createTodoItem("Todo Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("In Progress Task", "Description 2", LocalDate.now(), TodoItemStatus.IN_PROGRESS);
        createTodoItem("Completed Task", "Description 3", LocalDate.now(), TodoItemStatus.COMPLETED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.content[0].name").value("In Progress Task"));
    }

    @Test
    void shouldFilterTodoItemsByCancelledStatus() throws Exception {
        createTodoItem("Todo Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Cancelled Task", "Description 2", LocalDate.now(), TodoItemStatus.CANCELLED);
        createTodoItem("Completed Task", "Description 3", LocalDate.now(), TodoItemStatus.COMPLETED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.content[0].name").value("Cancelled Task"));
    }

    @Test
    void shouldFilterTodoItemsByDueDateFrom() throws Exception {
        LocalDate today = LocalDate.now();
        createTodoItem("Past Task", "Description 1", today.minusDays(2), TodoItemStatus.TODO);
        createTodoItem("Today Task", "Description 2", today, TodoItemStatus.TODO);
        createTodoItem("Future Task", "Description 3", today.plusDays(2), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("dueDateFrom", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Today Task", "Future Task")));
    }

    @Test
    void shouldFilterTodoItemsByDueDateTo() throws Exception {
        LocalDate today = LocalDate.now();
        createTodoItem("Past Task", "Description 1", today.minusDays(2), TodoItemStatus.TODO);
        createTodoItem("Today Task", "Description 2", today, TodoItemStatus.TODO);
        createTodoItem("Future Task", "Description 3", today.plusDays(2), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("dueDateTo", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Past Task", "Today Task")));
    }

    @Test
    void shouldFilterTodoItemsByDueDateRange() throws Exception {
        LocalDate today = LocalDate.now();
        createTodoItem("Past Task", "Description 1", today.minusDays(3), TodoItemStatus.TODO);
        createTodoItem("Start Range Task", "Description 2", today.minusDays(1), TodoItemStatus.TODO);
        createTodoItem("Middle Range Task", "Description 3", today, TodoItemStatus.TODO);
        createTodoItem("End Range Task", "Description 4", today.plusDays(1), TodoItemStatus.TODO);
        createTodoItem("Future Task", "Description 5", today.plusDays(3), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("dueDateFrom", today.minusDays(1).toString())
                .param("dueDateTo", today.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Start Range Task", "Middle Range Task", "End Range Task")));
    }

    @Test
    void shouldFilterTodoItemsByStatusAndDueDateRange() throws Exception {
        LocalDate today = LocalDate.now();
        createTodoItem("Todo Past", "Description 1", today.minusDays(1), TodoItemStatus.TODO);
        createTodoItem("Todo Today", "Description 2", today, TodoItemStatus.TODO);
        createTodoItem("Todo Future", "Description 3", today.plusDays(1), TodoItemStatus.TODO);
        createTodoItem("Completed Today", "Description 4", today, TodoItemStatus.COMPLETED);
        createTodoItem("Completed Future", "Description 5", today.plusDays(1), TodoItemStatus.COMPLETED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "TODO")
                .param("dueDateFrom", today.toString())
                .param("dueDateTo", today.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Todo Today", "Todo Future")))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("TODO"))));
    }

    @Test
    void shouldReturnEmptyWhenNoItemsMatchStatusFilter() throws Exception {
        createTodoItem("Todo Task", "Description 1", LocalDate.now(), TodoItemStatus.TODO);
        createTodoItem("Completed Task", "Description 2", LocalDate.now(), TodoItemStatus.COMPLETED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturnEmptyWhenNoItemsMatchDueDateFilter() throws Exception {
        LocalDate today = LocalDate.now();
        createTodoItem("Past Task", "Description 1", today.minusDays(5), TodoItemStatus.TODO);
        createTodoItem("Future Task", "Description 2", today.plusDays(5), TodoItemStatus.TODO);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("dueDateFrom", today.toString())
                .param("dueDateTo", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldCombineFilteringAndSorting() throws Exception {
        LocalDate today = LocalDate.now();
        createTodoItem("Z Todo Task", "Description 1", today, TodoItemStatus.TODO);
        createTodoItem("A Todo Task", "Description 2", today, TodoItemStatus.TODO);
        createTodoItem("M Todo Task", "Description 3", today, TodoItemStatus.TODO);
        createTodoItem("Completed Task", "Description 4", today, TodoItemStatus.COMPLETED);

        mockMvc.perform(get("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .param("status", "TODO")
                .param("dueDateFrom", today.toString())
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name").value("A Todo Task"))
                .andExpect(jsonPath("$.content[1].name").value("M Todo Task"))
                .andExpect(jsonPath("$.content[2].name").value("Z Todo Task"))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("TODO"))));
    }

    private void createTodoItem(String name, String description, LocalDate dueDate, TodoItemStatus status) throws Exception {
        TodoItemRequest request = new TodoItemRequest(name, description, dueDate, status);
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/items", testList.getId())
                .with(httpBasic(TestUser.EMAIL, TestUser.PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
