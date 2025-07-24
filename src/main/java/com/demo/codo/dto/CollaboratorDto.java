package com.demo.codo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Collaborator information for a todo list")
public class CollaboratorDto {

    @Schema(description = "User ID of the collaborator", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @Schema(description = "Name of the collaborator", example = "John Doe")
    private String userName;

    @Schema(description = "Email of the collaborator", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Whether the collaborator can edit the todo list", example = "true")
    private Boolean isEditable;
}
