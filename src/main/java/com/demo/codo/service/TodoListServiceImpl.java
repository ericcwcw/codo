package com.demo.codo.service;

import com.demo.codo.dto.TodoListRequest;
import com.demo.codo.dto.TodoListDto;
import com.demo.codo.entity.TodoList;
import com.demo.codo.exception.NotFoundException;
import com.demo.codo.mapper.TodoListMapper;
import com.demo.codo.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoListServiceImpl implements TodoListService {
    private final TodoListRepository repository;
    private final TodoListMapper mapper;

    @Override
    public TodoListDto create(TodoListRequest request) {
        TodoList newList = TodoList.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        TodoList savedList = repository.save(newList);
        return mapper.toDto(savedList);
    }

    @Override
    public Page<TodoListDto> getAll(Pageable pageable) {
        Page<TodoList> lists = repository.findAll(pageable);
        return lists.map(mapper::toDto);
    }

    @Override
    public Optional<TodoListDto> find(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    private TodoList getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Todo list not found, id=" + id));
    }

    @Override
    public TodoListDto update(UUID id, TodoListRequest request) {
        TodoList list = getOrThrow(id);
        if (request.getName() != null) {
            list.setName(request.getName());
        }
        if (request.getDescription() != null) {
            list.setDescription(request.getDescription());
        }
        TodoList updatedList = repository.save(list);
        return mapper.toDto(updatedList);
    }

    @Override
    public void delete(UUID id) {
        TodoList list = getOrThrow(id);
        repository.deleteById(list.getId());
    }
}
