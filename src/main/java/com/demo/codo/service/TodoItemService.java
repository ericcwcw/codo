package com.demo.codo.service;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TodoItemService {

    TodoItemDto create(UUID listId, TodoItemRequest request);

    Page<TodoItemDto> getAll(UUID listId, String status, LocalDate dueDateFrom, LocalDate dueDateTo, Pageable pageable);
    
    Optional<TodoItemDto> find(UUID listId, UUID id);

    TodoItemDto update(UUID listId, UUID id, TodoItemRequest request);

    void delete(UUID listId, UUID id);
}
