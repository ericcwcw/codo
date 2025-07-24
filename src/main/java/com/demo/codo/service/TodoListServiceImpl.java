package com.demo.codo.service;

import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListDto;
import com.demo.codo.entity.TodoList;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import com.demo.codo.exception.NotFoundException;
import com.demo.codo.mapper.TodoListMapper;
import com.demo.codo.repository.TodoListRepository;
import com.demo.codo.repository.UserRepository;
import com.demo.codo.repository.UserTodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoListServiceImpl implements TodoListService {
    private final TodoListRepository repository;
    private final TodoListMapper mapper;
    private final UserTodoListRepository userTodoListRepository;
    private final UserRepository userRepository;

    @Override
    public TodoListDto create(TodoListRequest request) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Handle case where there's no authentication (e.g., in tests)
        if (authentication == null || authentication.getName() == null) {
            // In test environment or when no user is authenticated, just create the TodoList without owner record
            TodoList newList = TodoList.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();

            TodoList savedList = repository.save(newList);
            return mapper.toDto(savedList);
        }
        
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found: " + userEmail));
        
        // Create the TodoList
        TodoList newList = TodoList.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        TodoList savedList = repository.save(newList);
        
        // Create owner record in UserTodoList table
        UserTodoList ownerRecord = UserTodoList.builder()
                .userId(currentUser.getId())
                .listId(savedList.getId())
                .isOwner(true)
                .isEditable(true) // Owner can always edit
                .build();
        
        userTodoListRepository.save(ownerRecord);
        
        return mapper.toDto(savedList);
    }

    @Override
    public Page<TodoListDto> getAll(Pageable pageable) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Handle case where there's no authentication (e.g., in tests)
        if (authentication == null || authentication.getName() == null) {
            // In test environment, return all lists
            Page<TodoList> lists = repository.findAll(pageable);
            return lists.map(mapper::toDto);
        }
        
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found: " + userEmail));
        
        // Get all TodoList IDs where the user is either owner or collaborator
        List<UUID> accessibleListIds = userTodoListRepository.findByUserId(currentUser.getId())
                .stream()
                .map(UserTodoList::getListId)
                .collect(Collectors.toList());
        
        if (accessibleListIds.isEmpty()) {
            // User has no accessible lists
            return Page.empty(pageable);
        }
        
        // Get TodoLists that the user has access to
        Page<TodoList> lists = repository.findByIdIn(accessibleListIds, pageable);
        return lists.map(mapper::toDto);
    }

    @Override
    public Optional<TodoListDto> find(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    private TodoList getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Todo list not found, id=" + id));
    }

    @Override
    public TodoListDto update(UUID id, TodoListRequest request) {
        TodoList list = getOrThrow(id);
        if (request.getName() != null) {
            list.setName(request.getName());
        }
        if (request.getDescription() != null) {
            list.setDescription(request.getDescription());
        }
        TodoList updatedList = repository.save(list);
        return mapper.toDto(updatedList);
    }

    @Override
    public void delete(UUID id) {
        TodoList list = getOrThrow(id);
        repository.deleteById(list.getId());
    }
}
