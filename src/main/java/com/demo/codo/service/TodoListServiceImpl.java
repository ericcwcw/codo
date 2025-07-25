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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        TodoList newList = TodoList.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        TodoList savedList = repository.save(newList);
        
        // Only create user-list relationship if user is authenticated and exists
        if (authentication != null && authentication.getName() != null) {
            String userEmail = authentication.getName();
            Optional<User> currentUserOpt = userRepository.findByEmail(userEmail);
            
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                UserTodoList ownerRecord = UserTodoList.builder()
                        .userId(currentUser.getId())
                        .listId(savedList.getId())
                        .isOwner(true)
                        .isEditable(true)
                        .build();
                
                userTodoListRepository.save(ownerRecord);
            }
        }
        
        return mapper.toDto(savedList);
    }

    @Override
    public Page<TodoListDto> getAll(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getName() == null) {
            Page<TodoList> lists = repository.findAll(pageable);
            return lists.map(mapper::toDto);
        }
        
        String userEmail = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(userEmail);
        
        if (currentUserOpt.isEmpty()) {
            // If user doesn't exist, return all lists (fallback behavior)
            Page<TodoList> lists = repository.findAll(pageable);
            return lists.map(mapper::toDto);
        }
        
        User currentUser = currentUserOpt.get();
        List<UUID> accessibleListIds = userTodoListRepository.findByUserId(currentUser.getId())
                .stream()
                .map(UserTodoList::getListId)
                .collect(Collectors.toList());
        
        if (accessibleListIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
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
