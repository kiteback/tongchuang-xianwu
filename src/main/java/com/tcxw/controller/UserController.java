package com.tcxw.controller;

import com.tcxw.annotation.RequireRole;
import com.tcxw.dto.LoginRequest;
import com.tcxw.dto.LoginResponse;
import com.tcxw.dto.RegisterRequest;
import com.tcxw.dto.UserResponse;
import com.tcxw.dto.UserUpdateRequest;
import com.tcxw.exception.ForbiddenException;
import com.tcxw.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        if ("USER".equals(role) && !userId.equals(id)) {
            throw new ForbiddenException("没有权限查看该用户");
        }
        return userService.getById(id);
    }

    @RequireRole("ADMIN")
    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userService.getAll();
    }

    @RequireRole("ADMIN")
    @PostMapping("/users")
    public UserResponse addUser(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @RequireRole("ADMIN")
    @PutMapping("/users/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request.getUsername(), request.getPassword());
    }
}
