package com.demo.codo.controller;

import com.demo.codo.annotation.RequireAccess;
import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListResponse;
import com.demo.codo.mapper.TodoListMapper;
import com.demo.codo.service.TodoListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Tag(name = "Todo Lists", description = "Operations for managing todo lists")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/todo/lists")
public class TodoListController {
    private final TodoListService service;
    private final TodoListMapper mapper;
    
    @Operation(summary = "Get all todo lists", description = "Retrieve a paginated list of all todo lists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved todo lists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<TodoListResponse>> getAll(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<TodoListResponse> listResponses = service.getAll(pageable).map(mapper::toResponse);
        return ResponseEntity.ok(listResponses);
    }

    @Operation(summary = "Create a new todo list", description = "Create a new todo list with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Todo list created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TodoListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<TodoListResponse> create(
            @Parameter(description = "Todo list information", required = true)
            @RequestBody TodoListRequest request) {
        TodoListDto listDto = service.create(request);
        TodoListResponse listResponse = mapper.toResponse(listDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(listResponse);
    }

    @Operation(summary = "Get todo list by ID", description = "Retrieve a specific todo list by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo list found and returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TodoListResponse.class))),
            @ApiResponse(responseCode = "404", description = "Todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @RequireAccess(value = RequireAccess.AccessType.READ, resourceIdParam = "id")
    public ResponseEntity<TodoListResponse> get(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID id) {
        return service.find(id).map(dto -> ResponseEntity.ok(mapper.toResponse(dto))).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update todo list", description = "Update an existing todo list with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo list updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TodoListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @PatchMapping("/{id}")
    @RequireAccess(value = RequireAccess.AccessType.EDIT, resourceIdParam = "id")
    public ResponseEntity<TodoListResponse> update(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated todo list information", required = true)
            @RequestBody TodoListRequest request) {
        TodoListDto listDto = service.update(id, request);
        TodoListResponse listResponse = mapper.toResponse(listDto);
        return ResponseEntity.ok(listResponse);
    }

    @Operation(summary = "Delete todo list", description = "Delete a todo list by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Todo list deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @RequireAccess(value = RequireAccess.AccessType.OWNER, resourceIdParam = "id")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
