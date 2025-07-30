package com.demo.codo.controller;

import com.demo.codo.annotation.RequireListPermission;
import com.demo.codo.annotation.RequireListPermission.Permission;
import com.demo.codo.dto.CollaboratorDto;
import com.demo.codo.dto.CollaboratorRequest;
import com.demo.codo.service.CollaboratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/todo/lists/{listId}/collaborators")
@RequiredArgsConstructor
@Tag(name = "Todo List Collaborators", description = "Operations for managing todo list collaborators")
@SecurityRequirement(name = "basicAuth")
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    @GetMapping
    @RequireListPermission(Permission.READ)
    @Operation(summary = "Get all collaborators for a todo list", description = "Retrieve all collaborators for a specific todo list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved collaborators"),
            @ApiResponse(responseCode = "404", description = "Todo list not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CollaboratorDto>> getCollaborators(
            @Parameter(description = "ID of the todo list", required = true)
            @PathVariable UUID listId) {
        List<CollaboratorDto> collaborators = collaboratorService.getCollaborators(listId);
        return ResponseEntity.ok(collaborators);
    }

    @PostMapping
    @RequireListPermission(Permission.OWNER)
    @Operation(summary = "Add a collaborator to a todo list", description = "Add a new collaborator to a specific todo list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Collaborator successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user already a collaborator"),
            @ApiResponse(responseCode = "404", description = "Todo list or user not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CollaboratorDto> addCollaborator(
            @Parameter(description = "ID of the todo list", required = true)
            @PathVariable UUID listId,
            @Valid @RequestBody CollaboratorRequest request) {
        CollaboratorDto collaborator = collaboratorService.addCollaborator(listId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(collaborator);
    }

    @PatchMapping("/{userId}")
    @RequireListPermission(Permission.OWNER)
    @Operation(summary = "Update a collaborator's permissions", description = "Update the permissions of an existing collaborator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collaborator successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Todo list or collaborator not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CollaboratorDto> updateCollaborator(
            @Parameter(description = "ID of the todo list", required = true)
            @PathVariable UUID listId,
            @Parameter(description = "ID of the user/collaborator", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody CollaboratorRequest request) {
        CollaboratorDto collaborator = collaboratorService.updateCollaborator(listId, userId, request);
        return ResponseEntity.ok(collaborator);
    }

    @DeleteMapping("/{userId}")
    @RequireListPermission(Permission.OWNER)
    @Operation(summary = "Remove a collaborator from a todo list", description = "Remove an existing collaborator from a specific todo list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Collaborator successfully removed"),
            @ApiResponse(responseCode = "404", description = "Todo list or collaborator not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> removeCollaborator(
            @Parameter(description = "ID of the todo list", required = true)
            @PathVariable UUID listId,
            @Parameter(description = "ID of the user/collaborator", required = true)
            @PathVariable UUID userId) {
        collaboratorService.removeCollaborator(listId, userId);
        return ResponseEntity.noContent().build();
    }
}
