package com.tcxw.service.impl;

import com.tcxw.dto.LoginResponse;
import com.tcxw.dto.RegisterRequest;
import com.tcxw.dto.UserResponse;
import com.tcxw.dto.UserUpdateRequest;
import com.tcxw.entity.User;
import com.tcxw.enums.UserStatus;
import com.tcxw.exception.BusinessException;
import com.tcxw.exception.NotFoundException;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.mapper.UserMapper;
import com.tcxw.service.UserService;
import com.tcxw.utils.JwtUtil;
import com.tcxw.utils.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String NULL_VALUE = "__NULL__";
    private static final Set<String> ROLES = Set.of("USER", "ADMIN");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final Duration userTtl;
    private final Duration nullTtl;

    public UserServiceImpl(UserMapper userMapper,
                           PasswordEncoder passwordEncoder,
                           StringRedisTemplate redisTemplate,
                           ObjectMapper objectMapper,
                           JwtUtil jwtUtil,
                           @Value("${app.cache.user-ttl:30m}") Duration userTtl,
                           @Value("${app.cache.null-ttl:2m}") Duration nullTtl) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.userTtl = userTtl;
        this.nullTtl = nullTtl;
    }

    @Override
    public UserResponse getById(Long id) {
        String key = RedisKeyUtil.userKey(id);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (NULL_VALUE.equals(json)) {
                throw new NotFoundException("用户不存在");
            }
            if (json != null) {
                return objectMapper.readValue(json, UserResponse.class);
            }
        } catch (NotFoundException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Redis read failed for key {}, falling back to MySQL", key, exception);
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            cacheValue(key, NULL_VALUE, nullTtl);
            throw new NotFoundException("用户不存在");
        }

        UserResponse response = UserResponse.from(user);
        try {
            cacheValue(key, objectMapper.writeValueAsString(response), userTtl);
        } catch (Exception exception) {
            log.warn("Redis write failed for key {}, returning MySQL result", key, exception);
        }
        return response;
    }

    @Override
    public List<UserResponse> getAll() {
        return userMapper.selectList(null).stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setPhone(blankToNull(request.phone()));
        user.setStatus(UserStatus.ACTIVE.getCode());
        user.setRole("USER");
        userMapper.insert(user);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new NotFoundException("用户不存在");
        }
        if (request.nickname() != null) {
            existing.setNickname(request.nickname());
        }
        if (request.phone() != null) {
            existing.setPhone(blankToNull(request.phone()));
        }
        if (request.password() != null && !request.password().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.status() != null) {
            if (request.status() != UserStatus.DISABLED.getCode()
                    && request.status() != UserStatus.ACTIVE.getCode()) {
                throw new BusinessException("用户状态只能是0或1");
            }
            existing.setStatus(request.status());
        }
        if (request.role() != null) {
            String role = request.role().toUpperCase();
            if (!ROLES.contains(role)) {
                throw new BusinessException("用户角色只能是USER或ADMIN");
            }
            existing.setRole(role);
        }
        userMapper.updateById(existing);
        evict(id);
        return UserResponse.from(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (userMapper.deleteById(id) == 0) {
            throw new NotFoundException("用户不存在");
        }
        evict(id);
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        if (!Integer.valueOf(UserStatus.ACTIVE.getCode()).equals(user.getStatus())) {
            throw new UnauthorizedException("用户已被禁用");
        }

        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole()));
        return response;
    }

    private void cacheValue(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (DataAccessException exception) {
            log.warn("Redis write failed for key {}", key, exception);
        }
    }

    private void evict(Long id) {
        String key = RedisKeyUtil.userKey(id);
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException exception) {
            log.warn("Redis eviction failed for key {}", key, exception);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
