package com.tcxw.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    @Test
    void readsClaimsFromValidToken() {
        JwtUtil jwt = new JwtUtil("this-is-a-test-secret-key-at-least-32-characters");
        String token = jwt.generateToken(3L, "alice", "USER");

        assertEquals(3L, jwt.getUserId(token));
        assertEquals("alice", jwt.getUsername(token));
        assertEquals("USER", jwt.getRole(token));
    }

    @Test
    void rejectsTamperedToken() {
        JwtUtil jwt = new JwtUtil("this-is-a-test-secret-key-at-least-32-characters");
        String token = jwt.generateToken(3L, "alice", "USER");
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThrows(Exception.class, () -> jwt.getUsername(tampered));
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtUtil jwt = new JwtUtil("this-is-a-test-secret-key-at-least-32-characters", 1L);
        String token = jwt.generateToken(3L, "alice", "USER");
        Thread.sleep(10L);

        assertThrows(Exception.class, () -> jwt.getUsername(token));
    }
}
