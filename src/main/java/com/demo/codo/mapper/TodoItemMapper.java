package com.demo.codo.mapper;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemResponse;
import com.demo.codo.entity.TodoItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoItemMapper {
    TodoItemResponse toResponse(TodoItemDto dto);
    TodoItemDto toDto(TodoItem entity);
}
