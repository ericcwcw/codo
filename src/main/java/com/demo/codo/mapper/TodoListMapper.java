package com.demo.codo.mapper;

import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListResponse;
import com.demo.codo.entity.TodoList;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoListMapper {
    TodoListResponse toResponse(TodoListDto list);
    
    TodoListDto toDto(TodoList list);
}
