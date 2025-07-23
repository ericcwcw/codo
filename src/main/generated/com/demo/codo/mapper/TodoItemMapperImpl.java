package com.demo.codo.mapper;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemResponse;
import com.demo.codo.entity.TodoItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-23T10:53:58+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class TodoItemMapperImpl implements TodoItemMapper {

    @Override
    public TodoItemResponse toResponse(TodoItemDto dto) {
        if ( dto == null ) {
            return null;
        }

        TodoItemResponse todoItemResponse = new TodoItemResponse();

        return todoItemResponse;
    }

    @Override
    public TodoItemDto toDto(TodoItem entity) {
        if ( entity == null ) {
            return null;
        }

        TodoItemDto todoItemDto = new TodoItemDto();

        return todoItemDto;
    }
}
