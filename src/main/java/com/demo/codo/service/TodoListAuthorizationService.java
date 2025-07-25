package com.demo.codo.service;

import com.demo.codo.annotation.RequireListPermission;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.repository.UserTodoListRepository;
import com.demo.codo.security.CustomUserDetailsService.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TodoListAuthorizationService {
    private final UserTodoListRepository userTodoListRepository;
    private final UserRepository userRepository;

    public void check(UUID listId, RequireListPermission.Permission permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || listId == null) {
            return;
        }

        try {
            UUID userId = getUserId(authentication);
            UserTodoList userTodoList = userTodoListRepository.findByUserIdAndListId(userId, listId)
                    .orElseThrow(() -> new AccessDeniedException("You do not have access to this todo list"));

            if (RequireListPermission.Permission.EDIT.equals(permission)) {
                if (!userTodoList.getIsOwner() && !userTodoList.getIsEditable()) {
                    throw new AccessDeniedException("You do not have permission to modify this resource. Read-only access.");
                }
            } else if (RequireListPermission.Permission.OWNER.equals(permission)) {
                if (!userTodoList.getIsOwner()) {
                    throw new AccessDeniedException("Only the owner can perform this operation");
                }
            }
        } catch (RuntimeException e) {
            // If user lookup fails, skip authorization (e.g., for create operations)
            return;
        }
    }
    
    private UUID getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found: " + userEmail));
        return user.getId();
    }
}
