package com.tcxw.service.impl;

import com.tcxw.dto.LoginResponse;
import com.tcxw.dto.UserResponse;
import com.tcxw.entity.User;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.service.UserService;
import com.tcxw.mapper.UserMapper;
import com.tcxw.utils.JwtUtil;
import com.tcxw.utils.RedisKeyUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            JwtUtil jwtUtil
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserResponse getById(Long id) {

        String key = RedisKeyUtil.userKey(id);

        try {
            // 1. 先从 Redis 查询
            String json = redisTemplate.opsForValue().get(key);

            if (json != null) {
                System.out.println("Redis命中：" + key);
                return objectMapper.readValue(json, UserResponse.class);
            }

            // 2. Redis 没有，再查询 MySQL
            System.out.println("Redis未命中：" + key);

            User user = userMapper.selectById(id);

            if (user == null) {
                return null;
            }

            // 3. 把 MySQL 数据转换成 UserResponse
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());

            // 4. 写入 Redis
            String responseJson = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(key, responseJson);

            System.out.println("Redis写入：" + key);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Redis缓存处理失败", e);
        }
    }

    @Override
    public List<UserResponse> getAll() {

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

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 1. 修改 MySQL
        userMapper.updateById(user);

        // 2. 删除 Redis 缓存
        String key = RedisKeyUtil.userKey(user.getId());
        redisTemplate.delete(key);

        System.out.println("Redis缓存删除：" + key);

        return user;
    }

    @Override
    public void delete(Long id) {

        userMapper.deleteById(id);

        // 删除用户时，同时删除 Redis 缓存
        String key = RedisKeyUtil.userKey(id);
        redisTemplate.delete(key);

        System.out.println("Redis缓存删除：" + key);
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

        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        response.setToken(token);

        return response;
    }
}

