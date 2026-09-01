package com.tcxw.service.impl;

import com.tcxw.dto.LoginResponse;
import com.tcxw.dto.UserResponse;
import com.tcxw.entity.User;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.service.UserService;
import org.springframework.stereotype.Service;
import com.tcxw.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.tcxw.utils.JwtUtil;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userMapper.selectById(id);

        if(user == null){
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());

        return response;
    }

    @Override
    public List<UserResponse> getAll(){
        List<User> users = userMapper.selectList(null);

        return users.stream().map(user -> {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            return response;

        }).toList();
    }

    @Override
    public UserResponse add(User user) {
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());

        return response;

    }

    @Override
    public User update(User user) {
        if(user.getPassword() != null && !user.getPassword().isBlank()){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userMapper.updateById(user);

        return user;
    }

    @Override
    public void delete(Long id){
        userMapper.deleteById(id);
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);

        if (user == null) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        LoginResponse response =new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());

        String token = JwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        response.setToken(token);

        return response;
    }
}
