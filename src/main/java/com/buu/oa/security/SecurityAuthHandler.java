package com.buu.oa.security;

import com.buu.oa.common.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Security认证/授权异常JSON响应处理器
 * 将401/403异常转换为统一R格式JSON响应，避免Spring Security默认重定向
 */
@Component
public class SecurityAuthHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuthHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("未认证请求: {} {}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), R.fail(401, "未登录或Token已过期，请重新登录"));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("权限不足: {} {} - {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), R.fail(403, "权限不足，无法访问该资源"));
    }
}
