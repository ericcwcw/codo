package com.demo.codo.mapper;

import com.demo.codo.dto.UserDto;
import com.demo.codo.dto.UserResponse;
import com.demo.codo.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User entity);
    UserResponse toResponse(User entity);
}
