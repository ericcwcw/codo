package com.demo.codo.mapper;

import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListResponse;
import com.demo.codo.entity.TodoList;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TodoListMapper {
    TodoListResponse toResponse(TodoListDto list);
    TodoListDto toDto(TodoList list);
}
