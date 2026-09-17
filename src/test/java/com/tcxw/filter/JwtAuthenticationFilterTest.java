package com.tcxw.filter;

import com.tcxw.entity.User;
import com.tcxw.exception.BusinessException;
import com.tcxw.mapper.UserMapper;
import com.tcxw.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @Test
    void returns401WhenAuthorizationHeaderIsMissing() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new JwtUtil("this-is-a-test-secret-key-at-least-32-characters"),
                mock(UserMapper.class),
                mock(ObjectMapper.class)
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders/my");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
    }

    @Test
    void doesNotConvertDownstreamBusinessExceptionTo401() throws Exception {
        JwtUtil jwt = new JwtUtil("this-is-a-test-secret-key-at-least-32-characters");
        UserMapper userMapper = mock(UserMapper.class);
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole("USER");
        user.setStatus(1);
        when(userMapper.selectById(1L)).thenReturn(user);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, userMapper, mock(ObjectMapper.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders/my");
        request.addHeader("Authorization", "Bearer " + jwt.generateToken(1L, "alice", "USER"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new BusinessException("订单状态错误")).when(chain).doFilter(request, response);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> filter.doFilter(request, response, chain)
        );

        assertEquals("订单状态错误", exception.getMessage());
    }
}
