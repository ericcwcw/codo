package com.demo.codo.controller;

import com.demo.codo.TestContainerConfiguration;
import com.demo.codo.dto.CollaboratorRequest;
import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.UserRequest;
import com.demo.codo.entity.TodoList;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import com.demo.codo.repository.TodoListRepository;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.repository.UserTodoListRepository;
import com.demo.codo.service.UserService;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainerConfiguration.class)
@Transactional
public class CollaboratorControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private UserTodoListRepository userTodoListRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final String TEST_NAME = "testuser";
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "testpass123";

    private User testUser;
    private User collaboratorUser;
    private TodoList testTodoList;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clean up
        userTodoListRepository.deleteAll();
        todoListRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user using UserService (for proper authentication)
        UserRequest newUserRequest = UserRequest.builder()
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .build();
        userService.create(newUserRequest);
        testUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();

        // Create collaborator user
        collaboratorUser = User.builder()
                .name("Collaborator User")
                .email("collaborator@example.com")
                .password("password")
                .build();
        collaboratorUser = userRepository.save(collaboratorUser);

        // Create test todo list
        testTodoList = TodoList.builder()
                .name("Test Todo List")
                .description("Test Description")
                .build();
        testTodoList = todoListRepository.save(testTodoList);

        // Make test user the owner
        UserTodoList ownerRelation = UserTodoList.builder()
                .userId(testUser.getId())
                .listId(testTodoList.getId())
                .isOwner(true)
                .isEditable(true)
                .build();
        userTodoListRepository.save(ownerRelation);
    }

    @Test
    void shouldGetEmptyCollaboratorsListInitially() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Empty - owners are excluded from collaborators list
    }

    @Test
    void shouldAddCollaboratorByEmail() throws Exception {
        CollaboratorRequest request = CollaboratorRequest.builder()
                .userId(collaboratorUser.getId())
                .canEdit(true)
                .build();

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(collaboratorUser.getId().toString())))
                .andExpect(jsonPath("$.userName", is(collaboratorUser.getName())))
                .andExpect(jsonPath("$.userEmail", is(collaboratorUser.getEmail())))
                .andExpect(jsonPath("$.isEditable", is(true)));
    }

    @Test
    void shouldAddCollaboratorWithReadOnlyPermission() throws Exception {
        CollaboratorRequest request = CollaboratorRequest.builder()
                .userId(collaboratorUser.getId())
                .canEdit(false)
                .build();

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(collaboratorUser.getId().toString())))
                .andExpect(jsonPath("$.userName", is(collaboratorUser.getName())))
                .andExpect(jsonPath("$.userEmail", is(collaboratorUser.getEmail())))
                .andExpect(jsonPath("$.isEditable", is(false)));
    }

    @Test
    void shouldFailToAddDuplicateCollaborator() throws Exception {
        // First, add the collaborator
        CollaboratorRequest request = CollaboratorRequest.builder()
                .userId(collaboratorUser.getId())
                .canEdit(true)
                .build();

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Try to add the same collaborator again
        mockMvc.perform(post("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateCollaboratorPermissions() throws Exception {
        // First, add a collaborator
        UserTodoList collaboration = UserTodoList.builder()
                .userId(collaboratorUser.getId())
                .listId(testTodoList.getId())
                .isOwner(false)
                .isEditable(false)
                .build();
        userTodoListRepository.save(collaboration);

        // Update permissions
        CollaboratorRequest request = CollaboratorRequest.builder()
                .canEdit(true)
                .build();

        mockMvc.perform(put("/api/v1/todo/lists/{listId}/collaborators/{userId}", 
                        testTodoList.getId(), collaboratorUser.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(collaboratorUser.getId().toString())))
                .andExpect(jsonPath("$.isEditable", is(true)));
    }

    @Test
    void shouldRemoveCollaborator() throws Exception {
        // First, add a collaborator
        UserTodoList collaboration = UserTodoList.builder()
                .userId(collaboratorUser.getId())
                .listId(testTodoList.getId())
                .isOwner(false)
                .isEditable(true)
                .build();
        userTodoListRepository.save(collaboration);

        // Remove the collaborator
        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/collaborators/{userId}", 
                        testTodoList.getId(), collaboratorUser.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isNoContent());

        // Verify collaborator is removed
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Empty - owners are excluded from collaborators list
    }


    @Test
    void shouldFailToAddCollaboratorWithInvalidEmail() throws Exception {
        CollaboratorRequest request = CollaboratorRequest.builder()
                .userId(UUID.randomUUID()) // Non-existent user ID
                .canEdit(true)
                .build();

        mockMvc.perform(post("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToUpdateNonexistentCollaborator() throws Exception {
        CollaboratorRequest request = CollaboratorRequest.builder()
                .canEdit(true)
                .build();

        mockMvc.perform(put("/api/v1/todo/lists/{listId}/collaborators/{userId}", 
                        testTodoList.getId(), UUID.randomUUID())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailToRemoveNonexistentCollaborator() throws Exception {
        mockMvc.perform(delete("/api/v1/todo/lists/{listId}/collaborators/{userId}", 
                        testTodoList.getId(), UUID.randomUUID())
                        .with(httpBasic(TEST_EMAIL, TEST_PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/todo/lists/{listId}/collaborators", testTodoList.getId()))
                .andExpect(status().isUnauthorized());
    }
}
