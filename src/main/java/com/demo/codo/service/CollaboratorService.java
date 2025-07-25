package com.demo.codo.service;

import com.demo.codo.dto.CollaboratorDto;
import com.demo.codo.dto.CollaboratorRequest;

import java.util.List;
import java.util.UUID;

public interface CollaboratorService {

    List<CollaboratorDto> getCollaborators(UUID listId);

    CollaboratorDto addCollaborator(UUID listId, CollaboratorRequest request);

    CollaboratorDto updateCollaborator(UUID listId, UUID userId, CollaboratorRequest request);

    void removeCollaborator(UUID listId, UUID userId);
}
