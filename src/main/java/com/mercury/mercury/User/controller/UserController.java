package com.mercury.mercury.User.controller;

import com.mercury.mercury.User.dto.CreateUserRequest;
import com.mercury.mercury.User.dto.UserResponse;
import com.mercury.mercury.User.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management Subsystem")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "POST Create User", description = "Validates username minimum boundaries, enforces secure regex passwords, hashes inputs with BCrypt, and spins up fresh corporate user records.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
