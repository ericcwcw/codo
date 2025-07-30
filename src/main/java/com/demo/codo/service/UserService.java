package com.demo.codo.service;

import com.demo.codo.dto.UserRequest;
import com.demo.codo.dto.UserResponse;
import com.demo.codo.entity.User;
import com.demo.codo.exception.DuplicateUserException;
import com.demo.codo.mapper.UserMapper;
import com.demo.codo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final UserMapper mapper;

    public User create(UserRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            log.debug("User with email {} already exists, skipping creation", request.getEmail());
            throw new DuplicateUserException("User with email " + request.getEmail() + " already exists");
        }
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .build();
        User savedUser = userRepository.save(newUser);

        try {
            emailVerificationService.sendVerificationEmail(savedUser);
            log.info("User created and verification email sent: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email for user: {}", savedUser.getEmail(), e);
        }
        return savedUser;
    }

    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email).map(mapper::toResponse);
    }
}
