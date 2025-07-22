package com.demo.codo.controller;

import com.demo.codo.dto.TodoItemRequest;
import com.demo.codo.dto.TodoItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/todo/lists/{listId}/items")
public class TodoItemController {

    @PostMapping
    public ResponseEntity<TodoItemResponse> createItem(
            @PathVariable UUID listId,
            @RequestBody TodoItemRequest request) {
        // TODO: Implement item creation
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping
    public ResponseEntity<Page<TodoItemResponse>> getItems(
            @PathVariable UUID listId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            Pageable pageable) {
        // TODO: Implement item retrieval with filtering and pagination
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoItemResponse> getItem(
            @PathVariable UUID listId, 
            @PathVariable UUID id) {
        // TODO: Implement get single item
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoItemResponse> updateItem(
            @PathVariable UUID listId, 
            @PathVariable UUID id,
            @RequestBody TodoItemRequest request) {
        // TODO: Implement item update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID listId, 
            @PathVariable UUID id) {
        // TODO: Implement item deletion
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
