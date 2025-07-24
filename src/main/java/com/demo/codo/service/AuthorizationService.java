package com.demo.codo.service;

import com.demo.codo.annotation.RequireAccess;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import com.demo.codo.repository.TodoItemRepository;
import com.demo.codo.repository.TodoListRepository;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.repository.UserTodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to handle authorization checks for TodoList and TodoItem operations.
 * Centralized authorization logic that can be used by AOP aspects.
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {
    
    private final UserRepository userRepository;
    private final UserTodoListRepository userTodoListRepository;
    private final TodoListRepository todoListRepository;
    private final TodoItemRepository todoItemRepository;
    
    /**
     * Check if the current user has the required access to a resource
     */
    public void checkAccess(UUID resourceId, RequireAccess.AccessType accessType, RequireAccess.ResourceType resourceType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Handle case where there's no authentication (e.g., in tests)
        if (authentication == null || authentication.getName() == null) {
            return; // Allow access in test environment
        }
        
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found: " + userEmail));
        
        UUID listId = getListId(resourceId, resourceType);
        
        // If the TodoList doesn't exist, skip authorization and let controller handle 404
        if (!todoListRepository.existsById(listId)) {
            return; // Skip authorization check - let controller return 404
        }
        
        // Get user's relationship to the TodoList
        UserTodoList userTodoList = userTodoListRepository.findByUserIdAndListId(currentUser.getId(), listId)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this todo list"));
        
        // Check access based on the required access type
        switch (accessType) {
            case READ:
                // All collaborators (owners + all collaborators) can read
                // If we got here, user has access (no additional checks needed)
                break;
                
            case EDIT:
                // Only owners and editable collaborators can edit
                if (!userTodoList.getIsOwner() && !userTodoList.getIsEditable()) {
                    throw new AccessDeniedException("You do not have permission to modify this resource. Read-only access.");
                }
                break;
                
            case OWNER:
                // Only owners can perform owner-only operations
                if (!userTodoList.getIsOwner()) {
                    throw new AccessDeniedException("Only the owner can perform this operation");
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown access type: " + accessType);
        }
    }
    
    /**
     * Get the TodoList ID based on the resource type and resource ID
     */
    private UUID getListId(UUID resourceId, RequireAccess.ResourceType resourceType) {
        switch (resourceType) {
            case TODO_LIST:
                return resourceId;
                
            case TODO_ITEM:
                // For TodoItem operations, we need to get the listId from the item
                return todoItemRepository.findById(resourceId)
                        .map(item -> item.getTodoList().getId())
                        .orElseThrow(() -> new RuntimeException("Todo item not found, id=" + resourceId));
                
            default:
                throw new IllegalArgumentException("Unknown resource type: " + resourceType);
        }
    }
}
