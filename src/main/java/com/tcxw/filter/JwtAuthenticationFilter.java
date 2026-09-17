package com.tcxw.filter;

import com.tcxw.entity.User;
import com.tcxw.enums.UserStatus;
import com.tcxw.exception.ErrorResponse;
import com.tcxw.mapper.UserMapper;
import com.tcxw.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserMapper userMapper, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        return HttpMethod.OPTIONS.matches(method)
                || (HttpMethod.POST.matches(method) && ("/login".equals(path) || "/register".equals(path)))
                || (HttpMethod.GET.matches(method) && (path.equals("/products") || path.startsWith("/products/")))
                || "/actuator/health".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, request, "请先登录");
            return;
        }

        User user;
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserId(token);
            String tokenUsername = jwtUtil.getUsername(token);
            user = userMapper.selectById(userId);

            if (user == null || !Integer.valueOf(UserStatus.ACTIVE.getCode()).equals(user.getStatus())) {
                writeUnauthorized(response, request, "用户不存在或已被禁用");
                return;
            }
            if (!user.getUsername().equals(tokenUsername)) {
                writeUnauthorized(response, request, "Token用户信息无效");
                return;
            }

        } catch (Exception exception) {
            log.debug("JWT validation failed", exception);
            writeUnauthorized(response, request, "Token无效或已过期");
            return;
        }

        // Keep downstream controller/service exceptions outside the JWT catch block.
        request.setAttribute("username", user.getUsername());
        request.setAttribute("role", user.getRole());
        request.setAttribute("userId", user.getId());
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   HttpServletRequest request,
                                   String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ErrorResponse.of("UNAUTHORIZED", message, request.getRequestURI())
        );
    }
}
