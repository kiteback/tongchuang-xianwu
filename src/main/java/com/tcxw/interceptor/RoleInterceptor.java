package com.tcxw.interceptor;

import com.tcxw.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler
    ) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        if (requireRole == null) {
            return true;
        }

        String role = (String) request.getAttribute("role");

        System.out.println("权限检查：当前角色 = " + role);
        System.out.println("权限检查：需要角色 = " + requireRole.value());

        if (!requireRole.value().equals(role)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"没有权限访问\"}");
            return false;
        }

        return true;
    }
}