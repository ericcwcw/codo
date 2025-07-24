package com.demo.codo.service.impl;

import com.demo.codo.dto.CollaboratorDto;
import com.demo.codo.dto.CollaboratorRequest;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import com.demo.codo.mapper.CollaboratorMapper;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.repository.UserTodoListRepository;
import com.demo.codo.service.CollaboratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaboratorServiceImpl implements CollaboratorService {

    private final UserTodoListRepository userTodoListRepository;
    private final UserRepository userRepository;
    private final CollaboratorMapper collaboratorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CollaboratorDto> getCollaborators(UUID listId) {
        // Only get collaborators (exclude owners)
        List<UserTodoList> userTodoLists = userTodoListRepository.findByListIdAndIsOwnerFalse(listId);
        
        return userTodoLists.stream()
                .map(utl -> {
                    // Fetch user for each collaboration
                    User user = userRepository.findById(utl.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found with id: " + utl.getUserId()));
                    
                    return collaboratorMapper.toDto(utl, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CollaboratorDto addCollaborator(UUID listId, CollaboratorRequest request) {
        UUID userId = request.getUserId();
        
        // Validate that userId is provided for add operations
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required for adding collaborators");
        }
        
        // Fetch the user to validate existence and set the relationship
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        // Check if collaboration already exists
        if (userTodoListRepository.existsByUserIdAndListId(userId, listId)) {
            throw new IllegalArgumentException("User is already a collaborator on this todo list");
        }
        
        UserTodoList userTodoList = UserTodoList.builder()
                .userId(userId)
                .listId(listId)
                .isEditable(request.getCanEdit())
                .build();
        
        UserTodoList saved = userTodoListRepository.save(userTodoList);
        
        // Create DTO using mapper with the user we already fetched
        return collaboratorMapper.toDto(saved, user);
    }

    @Override
    public CollaboratorDto updateCollaborator(UUID listId, UUID userId, CollaboratorRequest request) {
        UserTodoList userTodoList = userTodoListRepository.findByUserIdAndListId(userId, listId)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found"));
        
        // Don't allow changing owner status through this method
        userTodoList.setIsEditable(request.getCanEdit());
        
        UserTodoList saved = userTodoListRepository.save(userTodoList);
        
        // Fetch user for response
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return collaboratorMapper.toDto(saved, user);
    }

    @Override
    public void removeCollaborator(UUID listId, UUID userId) {
        if (!userTodoListRepository.existsByUserIdAndListId(userId, listId)) {
            throw new IllegalArgumentException("Collaboration not found");
        }
        
        userTodoListRepository.deleteByUserIdAndListId(userId, listId);
    }
}
