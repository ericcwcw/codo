package com.demo.codo.mapper;

import com.demo.codo.dto.TodoItemDto;
import com.demo.codo.dto.TodoItemResponse;
import com.demo.codo.entity.TodoItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-25T08:01:27+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class TodoItemMapperImpl implements TodoItemMapper {

    @Override
    public TodoItemResponse toResponse(TodoItemDto dto) {
        if ( dto == null ) {
            return null;
        }

        TodoItemResponse.TodoItemResponseBuilder todoItemResponse = TodoItemResponse.builder();

        todoItemResponse.id( dto.getId() );
        todoItemResponse.listId( dto.getListId() );
        todoItemResponse.name( dto.getName() );
        todoItemResponse.description( dto.getDescription() );
        todoItemResponse.dueDate( dto.getDueDate() );
        todoItemResponse.status( dto.getStatus() );
        todoItemResponse.createdAt( dto.getCreatedAt() );
        todoItemResponse.updatedAt( dto.getUpdatedAt() );

        return todoItemResponse.build();
    }

    @Override
    public TodoItemDto toDto(TodoItem entity) {
        if ( entity == null ) {
            return null;
        }

        TodoItemDto.TodoItemDtoBuilder todoItemDto = TodoItemDto.builder();

        todoItemDto.id( entity.getId() );
        todoItemDto.listId( entity.getListId() );
        todoItemDto.name( entity.getName() );
        todoItemDto.description( entity.getDescription() );
        todoItemDto.dueDate( entity.getDueDate() );
        todoItemDto.status( entity.getStatus() );
        todoItemDto.createdAt( entity.getCreatedAt() );
        todoItemDto.updatedAt( entity.getUpdatedAt() );

        return todoItemDto.build();
    }
}
