package com.demo.codo.mapper;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemResponse;
import com.demo.codo.entity.TodoItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TodoItemMapper {
    TodoItemResponse toResponse(TodoItemDto dto);
    TodoItemDto toDto(TodoItem entity);
}
