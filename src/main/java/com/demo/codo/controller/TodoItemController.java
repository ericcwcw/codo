package com.demo.codo.controller;

import com.demo.codo.annotation.RequireListPermission;
import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemRequest;
import com.demo.codo.dto.TodoItemResponse;
import com.demo.codo.enums.TodoItemStatus;
import com.demo.codo.mapper.TodoItemMapper;
import com.demo.codo.service.TodoItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Todo Items", description = "Operations for managing todo items within todo lists")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/todo/lists/{listId}/items")
public class TodoItemController {
    private final TodoItemService service;
    private final TodoItemMapper mapper;

    @Operation(summary = "Create a new todo item", description = "Create a new todo item within a specific todo list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Todo item created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TodoItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation error",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @PostMapping
    @RequireListPermission(RequireListPermission.Permission.EDIT)
    public ResponseEntity<TodoItemResponse> create(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID listId,
            @Parameter(description = "Todo item information", required = true)
            @Valid @RequestBody TodoItemRequest request) {
        TodoItemDto itemDto = service.create(listId, request);
        TodoItemResponse itemResponse = mapper.toResponse(itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponse);
    }

    @Operation(summary = "Get todo items", description = "Retrieve todo items from a specific list with optional filtering by status and due date range. Supports sorting by status, name, dueDate, createdAt, updatedAt (use ?sort=field,direction e.g. ?sort=status,asc&sort=name,desc)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved todo items",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @GetMapping
    @RequireListPermission(RequireListPermission.Permission.READ)
    public ResponseEntity<Page<TodoItemResponse>> getAll(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID listId,
            @Parameter(description = "Filter by item status (TODO, IN_PROGRESS, COMPLETED, CANCELLED)")
            @RequestParam(required = false) TodoItemStatus status,
            @Parameter(description = "Filter items with due date from this date (inclusive)")
            @RequestParam(required = false) LocalDate dueDateFrom,
            @Parameter(description = "Filter items with due date to this date (inclusive)")
            @RequestParam(required = false) LocalDate dueDateTo,
            @Parameter(description = "Pagination and sorting information. Sortable fields: status, name, dueDate, createdAt, updatedAt. Example: ?sort=status,asc&sort=name,desc&page=0&size=10")
            Pageable pageable) {
        Page<TodoItemDto> itemDtos = service.getAll(listId, status, dueDateFrom, dueDateTo, pageable);
        Page<TodoItemResponse> itemResponses = itemDtos.map(mapper::toResponse);
        return ResponseEntity.ok(itemResponses);
    }

    @Operation(summary = "Get todo item by ID", description = "Retrieve a specific todo item by its unique identifier within a todo list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo item found and returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TodoItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Todo item or todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @RequireListPermission(RequireListPermission.Permission.READ)
    public ResponseEntity<TodoItemResponse> get(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID listId,
            @Parameter(description = "Todo item unique identifier", required = true)
            @PathVariable UUID id) {
        return service.find(listId, id)
                .map(itemDto -> {
                    TodoItemResponse itemResponse = mapper.toResponse(itemDto);
                    return ResponseEntity.ok(itemResponse);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update todo item", description = "Update an existing todo item with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo item updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TodoItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation error",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Todo item or todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @PutMapping("/{id}")
    @RequireListPermission(RequireListPermission.Permission.EDIT)
    public ResponseEntity<TodoItemResponse> update(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID listId,
            @Parameter(description = "Todo item unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated todo item information", required = true)
            @RequestBody TodoItemRequest request) {
        TodoItemDto itemDto = service.update(listId, id, request);
        TodoItemResponse itemResponse = mapper.toResponse(itemDto);
        return ResponseEntity.ok(itemResponse);
    }

    @Operation(summary = "Delete todo item", description = "Delete a todo item by its unique identifier within a todo list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Todo item deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Todo item or todo list not found",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @RequireListPermission(RequireListPermission.Permission.EDIT)
    public ResponseEntity<Void> delete(
            @Parameter(description = "Todo list unique identifier", required = true)
            @PathVariable UUID listId,
            @Parameter(description = "Todo item unique identifier", required = true)
            @PathVariable UUID id) {
        service.delete(listId, id);
        return ResponseEntity.noContent().build();
    }
}
