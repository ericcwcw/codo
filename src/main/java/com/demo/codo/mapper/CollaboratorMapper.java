package com.demo.codo.mapper;

import com.demo.codo.dto.CollaboratorDto;
import com.demo.codo.entity.User;
import com.demo.codo.entity.UserTodoList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollaboratorMapper {
    @Mapping(source = "userTodoList.userId", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "userTodoList.isEditable", target = "isEditable")
    CollaboratorDto toDto(UserTodoList userTodoList, User user);
}
