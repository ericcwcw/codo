package com.demo.codo.repository;

import com.demo.codo.entity.UserTodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTodoListRepository extends JpaRepository<UserTodoList, UUID> {
    
    List<UserTodoList> findByListId(UUID listId);
    
    List<UserTodoList> findByListIdAndIsOwnerFalse(UUID listId);
    
    List<UserTodoList> findByUserId(UUID userId);
    
    Optional<UserTodoList> findByUserIdAndListId(UUID userId, UUID listId);
    
    @Query("SELECT utl FROM UserTodoList utl JOIN FETCH utl.user WHERE utl.listId = :listId")
    List<UserTodoList> findByListIdWithUser(@Param("listId") UUID listId);
    
    boolean existsByUserIdAndListId(UUID userId, UUID listId);
    
    void deleteByUserIdAndListId(UUID userId, UUID listId);
}
