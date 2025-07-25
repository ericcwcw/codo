package com.demo.codo.controller;

import com.demo.codo.dto.UserRequest;
import com.demo.codo.dto.UserResponse;
import com.demo.codo.entity.User;
import com.demo.codo.mapper.UserMapper;
import com.demo.codo.service.UserService;
import com.demo.codo.service.EmailVerificationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Tag(name = "Users", description = "Operations for managing users")
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;
    private final EmailVerificationService emailVerificationService;
    private final UserMapper userMapper;

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
    public ResponseEntity<UserResponse> create(
            @Parameter(description = "User details for creating a new account", required = true)
            @Valid @RequestBody UserRequest request) {
        User createdUser = service.create(request);
        UserResponse userResponse = userMapper.toResponse(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @Operation(summary = "Search user by email", description = "Find a user by their email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"User not found with email: user@example.com\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<UserResponse> searchByEmail(
            @Parameter(description = "Email address to search for", required = true, example = "john.doe@example.com")
            @RequestParam String email) {
        return service.findByEmail(email)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Verify user email", description = "Verify user email address using verification token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Email verified successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Invalid or expired verification token\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                    content = @Content)
    })
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "Verification token from email", required = true)
            @RequestParam(value = "token") String token) {
        boolean verified = emailVerificationService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok("{\"message\": \"Email verified successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"Invalid or expired verification token\"}");
        }
    }
}
