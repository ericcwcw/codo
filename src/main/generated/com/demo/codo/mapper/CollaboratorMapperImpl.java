package com.demo.codo.mapper;

import com.demo.codo.dto.CollaboratorDto;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-25T08:01:27+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class CollaboratorMapperImpl implements CollaboratorMapper {

    @Override
    public CollaboratorDto toDto(UserTodoList userTodoList, User user) {
        if ( userTodoList == null && user == null ) {
            return null;
        }

        CollaboratorDto.CollaboratorDtoBuilder collaboratorDto = CollaboratorDto.builder();

        if ( userTodoList != null ) {
            collaboratorDto.userId( userTodoList.getUserId() );
            collaboratorDto.isEditable( userTodoList.getIsEditable() );
        }
        if ( user != null ) {
            collaboratorDto.userName( user.getName() );
            collaboratorDto.userEmail( user.getEmail() );
        }

        return collaboratorDto.build();
    }
}
