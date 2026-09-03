package com.tcxw.utils;

public class RedisKeyUtil {

    public static String userKey(Long userId) {
        return "user:" + userId;
    }
}