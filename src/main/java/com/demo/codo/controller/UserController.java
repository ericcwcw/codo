package com.demo.codo.controller;

import com.demo.codo.dto.UserRequest;
import com.demo.codo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name = "Users", description = "Operations for managing users")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    @Operation(summary = "Create a new user", description = "Create a new user account with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data - validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Validation failed\", \"errors\": [\"Name is required\", \"Email must be valid\"]}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - User with this name or email already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"User with this name already exists\"}")))                    
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @Parameter(description = "User details for creating a new account", required = true)
            @Valid @RequestBody UserRequest request) {
        service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
