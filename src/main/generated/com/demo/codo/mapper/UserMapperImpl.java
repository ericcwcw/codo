package com.demo.codo.mapper;

import com.demo.codo.dto.UserDto;
import com.demo.codo.dto.UserResponse;
import com.demo.codo.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-25T08:01:27+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.id( entity.getId() );
        userDto.name( entity.getName() );
        userDto.email( entity.getEmail() );

        return userDto.build();
    }

    @Override
    public UserResponse toResponse(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( entity.getId() );
        userResponse.name( entity.getName() );
        userResponse.email( entity.getEmail() );
        userResponse.emailVerified( entity.getEmailVerified() );

        return userResponse.build();
    }
}
