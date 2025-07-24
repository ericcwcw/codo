package com.demo.codo.repository;

import com.demo.codo.entity.TodoList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, UUID> {
    
    Page<TodoList> findByIdIn(List<UUID> ids, Pageable pageable);
}
