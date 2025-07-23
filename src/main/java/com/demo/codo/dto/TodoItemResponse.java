package com.demo.codo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemResponse {
    private UUID id;
    private UUID listId;
    private String name;
    private String text;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
