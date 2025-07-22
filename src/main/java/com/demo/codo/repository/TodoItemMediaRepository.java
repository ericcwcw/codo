package com.demo.codo.repository;

import com.demo.codo.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TodoItemMediaRepository extends JpaRepository<TodoItem, UUID> {
}
