package com.demo.codo.repository;

import com.demo.codo.entity.TodoItem;
import com.demo.codo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, UUID> {
}
