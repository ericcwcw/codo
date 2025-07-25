package com.demo.codo.aspect;

import com.demo.codo.annotation.RequireAccess;
import com.demo.codo.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthorizationAspect {
    
    private final AuthorizationService authorizationService;

    @Before("@annotation(requireAccess)")
    public void checkAccess(JoinPoint joinPoint, RequireAccess requireAccess) {
        log.debug("Checking access for method: {}", joinPoint.getSignature().getName());
        
        try {
            UUID resourceId = extractResourceId(joinPoint, requireAccess.resourceIdParam());
            
            authorizationService.checkAccess(
                resourceId, 
                requireAccess.value(), 
                requireAccess.resourceType()
            );
            
            log.debug("Access granted for method: {} with resource ID: {}", 
                     joinPoint.getSignature().getName(), resourceId);
                     
        } catch (Exception e) {
            log.warn("Access denied for method: {} - {}", 
                    joinPoint.getSignature().getName(), e.getMessage());
            throw e;
        }
    }

    private UUID extractResourceId(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            if (parameter.getName().equals(parameterName)) {
                Object value = args[i];
                
                if (value instanceof UUID) {
                    return (UUID) value;
                } else if (value instanceof String) {
                    try {
                        return UUID.fromString((String) value);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid UUID format for parameter: " + parameterName);
                    }
                } else {
                    throw new IllegalArgumentException("Parameter " + parameterName + " must be of type UUID or String");
                }
            }
        }
        
        throw new IllegalArgumentException("Parameter '" + parameterName + "' not found in method signature");
    }
}
