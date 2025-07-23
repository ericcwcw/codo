package com.demo.codo.mapper;

import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListResponse;
import com.demo.codo.entity.TodoList;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-23T10:53:58+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class TodoListMapperImpl implements TodoListMapper {

    @Override
    public TodoListResponse toResponse(TodoListDto list) {
        if ( list == null ) {
            return null;
        }

        TodoListResponse todoListResponse = new TodoListResponse();

        return todoListResponse;
    }

    @Override
    public TodoListDto toDto(TodoList list) {
        if ( list == null ) {
            return null;
        }

        TodoListDto todoListDto = new TodoListDto();

        return todoListDto;
    }
}
