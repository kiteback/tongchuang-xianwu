package com.tcxw.service;

import com.tcxw.dto.LoginResponse;
import com.tcxw.dto.UserResponse;
import com.tcxw.entity.User;

import java.util.List;

public interface UserService {
    UserResponse getById(Long id);

    List<UserResponse> getAll();

    UserResponse add(User user);

    User update(User user);

    void delete(Long id);

    LoginResponse login(String username, String password);
}

