package com.demo.codo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify access requirements for TodoList operations.
 * Can be applied to controller methods to enforce authorization.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAccess {
    
    /**
     * The type of access required
     */
    AccessType value();
    
    /**
     * The parameter name that contains the resource ID (e.g., "listId", "id")
     * Defaults to "listId" for TodoList operations
     */
    String resourceIdParam() default "listId";
    
    /**
     * The type of resource being accessed
     */
    ResourceType resourceType() default ResourceType.TODO_LIST;
    
    enum AccessType {
        /**
         * Read access - all collaborators (owners + all collaborators) can view
         */
        READ,
        
        /**
         * Edit access - only owners and editable collaborators can modify
         */
        EDIT,
        
        /**
         * Owner access - only owners can perform this operation
         */
        OWNER
    }
    
    enum ResourceType {
        TODO_LIST,
        TODO_ITEM
    }
}
