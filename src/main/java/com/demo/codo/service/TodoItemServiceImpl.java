package com.demo.codo.service;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemRequest;
import com.demo.codo.entity.TodoItem;
import com.demo.codo.entity.TodoList;
import com.demo.codo.exception.NotFoundException;
import com.demo.codo.mapper.TodoItemMapper;
import com.demo.codo.repository.TodoItemRepository;
import com.demo.codo.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoItemServiceImpl implements TodoItemService {
    private final TodoItemRepository repository;
    private final TodoListRepository todoListRepository;
    private final TodoItemMapper mapper;

    @Override
    public TodoItemDto create(UUID listId, TodoItemRequest request) {
        // Validate that the todo list exists
        TodoList todoList = getTodoListOrThrow(listId);
        
        TodoItem newItem = TodoItem.builder()
                .name(request.getName())
                .text(request.getText())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .listId(listId)
                .todoList(todoList)
                .build();

        TodoItem savedItem = repository.save(newItem);
        return mapper.toDto(savedItem);
    }

    @Override
    public Page<TodoItemDto> getAll(UUID listId, String status, LocalDate dueDateFrom, LocalDate dueDateTo, Pageable pageable) {
        // Validate that the todo list exists
        getTodoListOrThrow(listId);
        
        Specification<TodoItem> spec = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
        
        // Filter by list ID
        spec = spec.and((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("todoList").get("id"), listId));
        
        // Filter by status if provided
        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("status"), status));
        }
        
        // Filter by due date range if provided
        if (dueDateFrom != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
        }
        if (dueDateTo != null) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
        }
        
        Page<TodoItem> items = repository.findAll(spec, pageable);
        return items.map(mapper::toDto);
    }

    @Override
    public Optional<TodoItemDto> get(UUID listId, UUID id) {
        // Validate that the todo list exists
        getTodoListOrThrow(listId);
        
        return repository.findByIdAndTodoListId(id, listId).map(mapper::toDto);
    }

    @Override
    public TodoItemDto update(UUID listId, UUID id, TodoItemRequest request) {
        // Validate that the todo list exists
        getTodoListOrThrow(listId);
        
        TodoItem item = getTodoItemOrThrow(listId, id);
        
        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getText() != null) {
            item.setText(request.getText());
        }
        if (request.getDueDate() != null) {
            item.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        
        TodoItem updatedItem = repository.save(item);
        return mapper.toDto(updatedItem);
    }

    @Override
    public void delete(UUID listId, UUID id) {
        // Validate that the todo list exists
        getTodoListOrThrow(listId);
        
        TodoItem item = getTodoItemOrThrow(listId, id);
        repository.deleteById(item.getId());
    }

    private TodoList getTodoListOrThrow(UUID listId) {
        return todoListRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("Todo list not found, id=" + listId));
    }

    private TodoItem getTodoItemOrThrow(UUID listId, UUID id) {
        return repository.findByIdAndTodoListId(id, listId)
                .orElseThrow(() -> new NotFoundException("Todo item not found, id=" + id + ", listId=" + listId));
    }
}
