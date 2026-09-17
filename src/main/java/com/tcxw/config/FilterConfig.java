package com.tcxw.config;

import com.tcxw.filter.JwtAuthenticationFilter;
import com.tcxw.mapper.UserMapper;
import com.tcxw.utils.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(JwtUtil jwtUtil,
                                                                       UserMapper userMapper,
                                                                       ObjectMapper objectMapper) {

        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new JwtAuthenticationFilter(jwtUtil, userMapper, objectMapper));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }
}
