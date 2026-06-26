package com.buu.oa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置
 * 放行静态资源与考勤日历API，其余接口预留JWT认证扩展点
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 安全过滤链配置
     * 关闭CSRF、启用无状态会话，放行静态资源与考勤API，开发阶段放行所有请求
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain安全过滤链
     * @throws Exception 配置异常时抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/static/**", "/index.html", "/api/attendance/**").permitAll()
                .anyRequest().permitAll() // 开发阶段放行所有接口，后续集成JWT时收紧
            );
        return http.build();
    }
}
