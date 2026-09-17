package com.tcxw.interceptor;

import com.tcxw.annotation.RequireRole;
import com.tcxw.exception.ForbiddenException;
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

        if (!requireRole.value().equals(role)) {
            throw new ForbiddenException("没有权限访问");
        }

        return true;
    }
}
