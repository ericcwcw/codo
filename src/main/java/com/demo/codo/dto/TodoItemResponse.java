package com.demo.codo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object containing todo item information")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemResponse {
    @Schema(description = "Unique identifier of the todo item", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "Unique identifier of the parent todo list", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID listId;
    
    @Schema(description = "Name of the todo item", example = "Complete project documentation")
    private String name;
    
    @Schema(description = "Detailed description of the todo item", example = "Write comprehensive documentation for the Spring Boot project")
    private String description;
    
    @Schema(description = "Due date for the todo item", example = "2024-12-31", type = "string", format = "date")
    private LocalDate dueDate;
    
    @Schema(description = "Current status of the todo item", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"})
    private String status;
    
    @Schema(description = "Timestamp when the todo item was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the todo item was last updated", example = "2024-01-15T14:45:00")
    private LocalDateTime updatedAt;
}
