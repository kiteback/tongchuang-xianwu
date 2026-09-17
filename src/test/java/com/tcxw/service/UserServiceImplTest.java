package com.tcxw.service;

import com.tcxw.dto.UserResponse;
import com.tcxw.entity.User;
import com.tcxw.exception.UnauthorizedException;
import com.tcxw.mapper.UserMapper;
import com.tcxw.service.impl.UserServiceImpl;
import com.tcxw.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Test
    void fallsBackToMySqlWhenRedisIsUnavailable() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("user:7")).thenThrow(new DataAccessResourceFailureException("redis down"));

        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        user.setNickname("Alice");
        user.setRole("USER");
        user.setStatus(1);
        when(userMapper.selectById(7L)).thenReturn(user);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        UserServiceImpl service = service(userMapper, encoder, redis, objectMapper);
        UserResponse response = service.getById(7L);

        assertEquals("alice", response.getUsername());
    }

    @Test
    void rejectsDisabledUserAtLogin() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        User user = new User();
        user.setUsername("alice");
        user.setPassword("hash");
        user.setStatus(0);
        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(encoder.matches("password", "hash")).thenReturn(true);

        UserServiceImpl service = service(
                userMapper, encoder, mock(StringRedisTemplate.class), mock(ObjectMapper.class));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> service.login("alice", "password")
        );
        assertEquals("用户已被禁用", exception.getMessage());
    }

    @Test
    void usesSameMessageForUnknownUserAndWrongPassword() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserServiceImpl service = service(
                userMapper, encoder, mock(StringRedisTemplate.class), mock(ObjectMapper.class));

        when(userMapper.findByUsername(anyString())).thenReturn(null);
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> service.login("missing", "wrong")
        );
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    private static UserServiceImpl service(UserMapper userMapper,
                                           PasswordEncoder encoder,
                                           StringRedisTemplate redis,
                                           ObjectMapper objectMapper) {
        return new UserServiceImpl(
                userMapper,
                encoder,
                redis,
                objectMapper,
                new JwtUtil("this-is-a-test-secret-key-at-least-32-characters"),
                Duration.ofMinutes(30),
                Duration.ofMinutes(2)
        );
    }
}
