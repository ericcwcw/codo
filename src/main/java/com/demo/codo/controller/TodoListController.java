package com.demo.codo.controller;

import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListResponse;
import com.demo.codo.mapper.TodoListMapper;
import com.demo.codo.service.TodoListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/todo/lists")
public class TodoListController {
    private final TodoListService service;
    private final TodoListMapper mapper;
    
    @GetMapping
    public ResponseEntity<Page<TodoListResponse>> getAll(Pageable pageable) {
        Page<TodoListResponse> listResponses = service.getAll(pageable).map(mapper::toResponse);
        return ResponseEntity.ok(listResponses);
    }

    @PostMapping
    public ResponseEntity<TodoListResponse> create(@RequestBody TodoListRequest request) {
        TodoListDto listDto = service.create(request);
        TodoListResponse listResponse = mapper.toResponse(listDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(listResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoListResponse> get(@PathVariable UUID id) {
        return service.get(id).map(dto -> ResponseEntity.ok(mapper.toResponse(dto))).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TodoListResponse> update(@PathVariable UUID id, @RequestBody TodoListRequest request) {
        TodoListDto listDto = service.update(id, request);
        TodoListResponse listResponse = mapper.toResponse(listDto);
        return ResponseEntity.ok(listResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
