package com.demo.codo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new user")
public class UserRequest {
    
    @Schema(description = "User's display name", 
            example = "john_doe", 
            required = true,
            minLength = 1,
            maxLength = 50)
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;
    
    @Schema(description = "User's email address", 
            example = "john.doe@example.com", 
            required = true,
            format = "email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Schema(description = "User's password", 
            example = "SecurePassword123!", 
            required = true,
            minLength = 8,
            maxLength = 100)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}
