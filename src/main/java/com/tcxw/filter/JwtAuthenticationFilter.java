package com.tcxw.filter;

import com.tcxw.utils.JwtUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JwtAuthenticationFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");

        String path = httpRequest.getRequestURI();

        if ("/login".equals(path)
                || "/register".equals(path)
                || "/redis-get".equals(path)
                || "/redis-test".equals(path)) {

            chain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"message\":\"请先登录\"}");
            return;
        }

        String username;
        String role;
        Long userId;

        try {
            String token = authHeader.substring(7);

            username = jwtUtil.getUsername(token);
            role = jwtUtil.getRole(token);
            userId = jwtUtil.getUserId(token);

        } catch (Exception e) {

            System.out.println("========== JWT验证异常 ==========");
            e.printStackTrace();
            System.out.println("================================");

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"message\":\"Token无效或已过期\"}");
            return;
        }

        System.out.println("Jwt用户：" + username);
        httpRequest.setAttribute("username", username);

        System.out.println("Jwt角色：" + role);
        httpRequest.setAttribute("role", role);

        System.out.println("Jwt用户ID：" + userId);
        httpRequest.setAttribute("userId", userId);

        chain.doFilter(request, response);
    }
}
