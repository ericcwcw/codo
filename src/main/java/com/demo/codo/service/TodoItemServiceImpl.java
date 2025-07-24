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
        TodoList todoList = getTodoList(listId);
        TodoItem newItem = TodoItem.builder()
                .name(request.getName())
                .description(request.getDescription())
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
        getTodoList(listId);
        Specification<TodoItem> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("todoList").get("id"), listId);
            
        if (status != null && !status.trim().isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("status"), status));
        }
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
    public Optional<TodoItemDto> find(UUID listId, UUID id) {
        getTodoList(listId);
        return repository.findByIdAndTodoListId(id, listId).map(mapper::toDto);
    }

    @Override
    public TodoItemDto update(UUID listId, UUID id, TodoItemRequest request) {
        getTodoList(listId);
        TodoItem item = get(listId, id);
        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
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
        getTodoList(listId);
        TodoItem item = get(listId, id);
        repository.deleteById(item.getId());
    }

    private TodoList getTodoList(UUID listId) {
        return todoListRepository.findById(listId)
                .orElseThrow(() -> new NotFoundException("Todo list not found, id=" + listId));
    }

    private TodoItem get(UUID listId, UUID id) {
        return repository.findByIdAndTodoListId(id, listId)
                .orElseThrow(() -> new NotFoundException("Todo item not found, id=" + id + ", listId=" + listId));
    }
}
