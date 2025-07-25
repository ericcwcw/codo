package com.demo.codo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireListPermission {
    Permission value();
    
    enum Permission {
        READ,   // Any collaborator can read
        EDIT,   // Owner or editable collaborator can edit
        OWNER   // Only owner can perform this action
    }
}
