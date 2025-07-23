package com.demo.codo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object containing todo list information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoListResponse {
    @Schema(description = "Unique identifier of the todo list", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Name of the todo list", example = "Work Tasks")
    private String name;
    
    @Schema(description = "Description of the todo list", example = "Tasks related to work projects")
    private String description;
    
    @Schema(description = "Timestamp when the todo list was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the todo list was last updated", example = "2024-01-15T14:45:00")
    private LocalDateTime updatedAt;
}
