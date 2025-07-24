package com.demo.codo.service;

import com.demo.codo.dto.CollaboratorDto;
import com.demo.codo.dto.CollaboratorRequest;

import java.util.List;
import java.util.UUID;

public interface CollaboratorService {
    
    /**
     * Get all collaborators for a todo list
     */
    List<CollaboratorDto> getCollaborators(UUID listId);
    
    /**
     * Add a collaborator to a todo list
     */
    CollaboratorDto addCollaborator(UUID listId, CollaboratorRequest request);
    
    /**
     * Update a collaborator's permissions
     */
    CollaboratorDto updateCollaborator(UUID listId, UUID userId, CollaboratorRequest request);
    
    /**
     * Remove a collaborator from a todo list
     */
    void removeCollaborator(UUID listId, UUID userId);
}
