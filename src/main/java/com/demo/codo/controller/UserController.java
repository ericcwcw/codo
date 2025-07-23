package com.demo.codo.controller;

import com.demo.codo.dto.UserRequest;
import com.demo.codo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    @PostMapping
    public void create(@RequestBody UserRequest request) {
        service.create(request);
    }
}
