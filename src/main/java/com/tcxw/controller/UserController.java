package com.tcxw.controller;

import com.tcxw.dto.LoginResponse;
import com.tcxw.dto.UserResponse;
import com.tcxw.entity.User;
import com.tcxw.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RestController;
import com.tcxw.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.tcxw.dto.LoginRequest;
import com.tcxw.annotation.RequireRole;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id, HttpServletRequest request){

        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");

        System.out.println("Controller当前用户:" + username );
        System.out.println("Controller当前角色:" + role );
        System.out.println("Controller当前用户ID:" + userId );

        if("USER".equals(role) && !userId.equals(id)){
            throw new UnauthorizedException("没有权限查看该用户");
        }

        return userService.getById(id);
    }

    @RequireRole("ADMIN")
    @GetMapping("/users")
    public List<UserResponse> getAllUsers(){
        return userService.getAll();
    }

    @RequireRole("ADMIN")
    @PostMapping("/users")
    public UserResponse addUser(@RequestBody User user){
        return userService.add(user);
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody User user){
        return userService.add(user);
    }

    @RequireRole("ADMIN")
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user){
        user.setId(id);
        return userService.update(user);
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/users/{id}")
    public void deleteuser(
            @PathVariable Long id)
    {
        userService.delete(id);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return userService.login(
                request.getUsername(),request.getPassword()
        );

    }

}
