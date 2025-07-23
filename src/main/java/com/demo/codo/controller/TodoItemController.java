package com.demo.codo.controller;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemRequest;
import com.demo.codo.dto.TodoItemResponse;
import com.demo.codo.mapper.TodoItemMapper;
import com.demo.codo.service.TodoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/todo/lists/{listId}/items")
public class TodoItemController {
    private final TodoItemService service;
    private final TodoItemMapper mapper;

    @PostMapping
    public ResponseEntity<TodoItemResponse> createItem(
            @PathVariable UUID listId,
            @Valid @RequestBody TodoItemRequest request) {
        TodoItemDto itemDto = service.create(listId, request);
        TodoItemResponse itemResponse = mapper.toResponse(itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponse);
    }

    @GetMapping
    public ResponseEntity<Page<TodoItemResponse>> getItems(
            @PathVariable UUID listId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            Pageable pageable) {
        Page<TodoItemDto> itemDtos = service.getAll(listId, status, dueDateFrom, dueDateTo, pageable);
        Page<TodoItemResponse> itemResponses = itemDtos.map(mapper::toResponse);
        return ResponseEntity.ok(itemResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoItemResponse> getItem(
            @PathVariable UUID listId, 
            @PathVariable UUID id) {
        return service.get(listId, id)
                .map(itemDto -> {
                    TodoItemResponse itemResponse = mapper.toResponse(itemDto);
                    return ResponseEntity.ok(itemResponse);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoItemResponse> updateItem(
            @PathVariable UUID listId, 
            @PathVariable UUID id,
            @RequestBody TodoItemRequest request) {
        TodoItemDto itemDto = service.update(listId, id, request);
        TodoItemResponse itemResponse = mapper.toResponse(itemDto);
        return ResponseEntity.ok(itemResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID listId, 
            @PathVariable UUID id) {
        service.delete(listId, id);
        return ResponseEntity.noContent().build();
    }
}
