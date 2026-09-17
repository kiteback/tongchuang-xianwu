package com.tcxw.service;

import com.tcxw.dto.LoginResponse;
import com.tcxw.dto.RegisterRequest;
import com.tcxw.dto.UserUpdateRequest;
import com.tcxw.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getById(Long id);

    List<UserResponse> getAll();

    UserResponse register(RegisterRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);

    LoginResponse login(String username, String password);
}

