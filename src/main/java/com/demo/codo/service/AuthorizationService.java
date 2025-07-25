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

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    
    private final UserRepository userRepository;
    private final UserTodoListRepository userTodoListRepository;
    private final TodoListRepository todoListRepository;
    private final TodoItemRepository todoItemRepository;

    public void checkAccess(UUID resourceId, RequireAccess.AccessType accessType, RequireAccess.ResourceType resourceType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getName() == null) {
            return; 
        }
        
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found: " + userEmail));
        
        UUID listId = getListId(resourceId, resourceType);
        
        if (!todoListRepository.existsById(listId)) {
            return; 
        }
        
        UserTodoList userTodoList = userTodoListRepository.findByUserIdAndListId(currentUser.getId(), listId)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this todo list"));

        if (accessType == RequireAccess.AccessType.EDIT) {
            if (!userTodoList.getIsOwner() && !userTodoList.getIsEditable()) {
                throw new AccessDeniedException("You do not have permission to modify this resource. Read-only access.");
            }
        } else if (accessType == RequireAccess.AccessType.OWNER) {
            if (!userTodoList.getIsOwner()) {
                throw new AccessDeniedException("Only the owner can perform this operation");
            }
        }
    }

    private UUID getListId(UUID resourceId, RequireAccess.ResourceType resourceType) {
        if (resourceType == RequireAccess.ResourceType.TODO_LIST) {
            return resourceId;
        } else if (resourceType == RequireAccess.ResourceType.TODO_ITEM) {
            return todoItemRepository.findById(resourceId)
                    .map(item -> item.getTodoList().getId())
                    .orElseThrow(() -> new RuntimeException("Todo item not found, id=" + resourceId));
        }
        return null;
    }
}
