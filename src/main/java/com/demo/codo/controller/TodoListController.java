package com.demo.codo.controller;

import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/todo/lists")
public class TodoListController {
    
    @GetMapping
    public ResponseEntity<Page<TodoListResponse>> getLists(Pageable pageable) {
        // TODO: Implement list retrieval with filtering and pagination
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping
    public ResponseEntity<TodoListResponse> createList(@RequestBody TodoListRequest request) {
        // TODO: Implement list creation
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoListResponse> getList(@PathVariable UUID id) {
        // TODO: Implement get single list
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoListResponse> updateList(
            @PathVariable UUID id, 
            @RequestBody TodoListRequest request) {
        // TODO: Implement list update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable UUID id) {
        // TODO: Implement list deletion
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
