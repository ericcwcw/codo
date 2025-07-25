package com.demo.codo.aspect;

import com.demo.codo.annotation.RequireListPermission;
import com.demo.codo.service.TodoListAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class ListPermissionAspect {

    public static final String LIST_ID = "listId";
    private final TodoListAuthorizationService authorizationService;

    @Before("@annotation(requireListPermission)")
    public void checkListPermission(JoinPoint joinPoint, RequireListPermission requireListPermission) {
        UUID listId = getListId(joinPoint);
        if (listId != null) {
            RequireListPermission.Permission permission = requireListPermission.value();
            authorizationService.check(listId, permission);
        }
    }

    private UUID getListId(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            if (LIST_ID.equals(parameters[i].getName())) {
                return (UUID) args[i];
            }
        }
        return null;
    }
}
