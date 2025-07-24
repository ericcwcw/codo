package com.demo.codo.service;

import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TodoListService {

    TodoListDto create(TodoListRequest request);

    Page<TodoListDto> getAll(Pageable pageable);
    
    Optional<TodoListDto> find(UUID id);

    TodoListDto update(UUID id, TodoListRequest request);

    void delete(UUID id);
}
