package com.demo.codo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemRequest {
    private String name;
    private String text;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private String status;
    private UUID listId;
}
