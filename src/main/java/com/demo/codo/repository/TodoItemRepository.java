package com.demo.codo.repository;

import com.demo.codo.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, UUID>, JpaSpecificationExecutor<TodoItem> {
    
    Optional<TodoItem> findByIdAndTodoListId(UUID id, UUID todoListId);
}
