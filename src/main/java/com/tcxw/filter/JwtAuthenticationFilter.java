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

        if ("/login".equals(path) || "/register".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"请先登录\"}");
            return;
        }
        try{
            String token = authHeader.substring(7);
            String username = JwtUtil.getUsername(token);
            System.out.println("Jwt用户：" + username);
            httpRequest.setAttribute("username",username);

            String role = JwtUtil.getRole(token);
            System.out.println("Jwt角色："+role);
            httpRequest.setAttribute("role",role);

            Long userId = JwtUtil.getUserId(token);
            System.out.println("Jwt用户ID：" + userId);
            httpRequest.setAttribute("userId",userId);

            chain.doFilter(request, response);
        }catch (Exception e){
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"message\":\"Token无效或已过期\"}");

        }
    }
}

