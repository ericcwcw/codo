package com.demo.codo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to add or update a collaborator")
public class CollaboratorRequest {

    @Schema(description = "User ID of the collaborator (required for add operations)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;

    @NotNull
    @Schema(description = "Whether the collaborator can edit the todo list", example = "true")
    private Boolean canEdit;
}
