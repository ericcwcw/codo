package com.demo.codo.mapper;

import com.demo.codo.dto.TodoListDto;
import com.demo.codo.dto.TodoListResponse;
import com.demo.codo.entity.TodoList;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-25T08:01:27+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class TodoListMapperImpl implements TodoListMapper {

    @Override
    public TodoListResponse toResponse(TodoListDto list) {
        if ( list == null ) {
            return null;
        }

        TodoListResponse.TodoListResponseBuilder todoListResponse = TodoListResponse.builder();

        todoListResponse.id( list.getId() );
        todoListResponse.name( list.getName() );
        todoListResponse.description( list.getDescription() );
        todoListResponse.createdAt( list.getCreatedAt() );
        todoListResponse.updatedAt( list.getUpdatedAt() );

        return todoListResponse.build();
    }

    @Override
    public TodoListDto toDto(TodoList list) {
        if ( list == null ) {
            return null;
        }

        TodoListDto.TodoListDtoBuilder todoListDto = TodoListDto.builder();

        todoListDto.id( list.getId() );
        todoListDto.name( list.getName() );
        todoListDto.description( list.getDescription() );
        todoListDto.createdAt( list.getCreatedAt() );
        todoListDto.updatedAt( list.getUpdatedAt() );

        return todoListDto.build();
    }
}
