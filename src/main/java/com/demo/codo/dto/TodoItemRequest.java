package com.demo.codo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemRequest {
    private String name;
    private String text;
    private LocalDate dueDate;
    private String status;
    private UUID listId;
}
