package com.demo.codo.dto;

import com.demo.codo.enums.TodoItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request object for creating or updating a todo item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemRequest {
    @Schema(description = "Name of the todo item", example = "Complete project documentation", required = true, maxLength = 255)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Schema(description = "Detailed description of the todo item", example = "Write comprehensive documentation for the Spring Boot project")
    private String description;
    
    @Schema(description = "Due date for the todo item", example = "2024-12-31", type = "string", format = "date")
    private LocalDate dueDate;

    @Schema(description = "Status of the todo item", example = "TODO", allowableValues = {"TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED"}, required = true)
    @NotNull(message = "Status is required")
    private TodoItemStatus status;
}
