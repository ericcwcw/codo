package com.demo.codo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request object for creating or updating a todo list")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoListRequest {
    @Schema(description = "Name of the todo list", example = "Work Tasks", required = true)
    private String name;
    
    @Schema(description = "Description of the todo list", example = "Tasks related to work projects")
    private String description;
}
