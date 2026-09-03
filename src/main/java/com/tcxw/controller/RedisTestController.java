package com.tcxw.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    public RedisTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis-test")
    public String testRedis(@RequestParam String value) {
        redisTemplate.opsForValue().set("test", value);
        return "Redis写入成功：" + value;
    }

    @GetMapping("/redis-get")
    public String getRedis() {
        String value = redisTemplate.opsForValue().get("test");
        return "Redis读取结果：" + value;
    }
}