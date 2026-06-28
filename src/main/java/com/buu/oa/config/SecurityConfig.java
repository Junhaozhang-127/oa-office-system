package com.buu.oa.config;

import com.buu.oa.security.JwtAuthenticationFilter;
import com.buu.oa.security.SecurityAuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置
 * 配置JWT无状态认证、接口权限规则、密码加密器
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityAuthHandler securityAuthHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          SecurityAuthHandler securityAuthHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityAuthHandler = securityAuthHandler;
    }

    /**
     * 安全过滤链配置
     * 关闭CSRF、无状态会话、JWT过滤器、接口权限控制
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常时抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(securityAuthHandler)
                .accessDeniedHandler(securityAuthHandler))
            .authorizeHttpRequests(auth -> auth
                // 登录接口和静态资源放行
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/static/**", "/index.html", "/pages/**", "/css/**", "/js/**").permitAll()
                // 认证相关接口需要登录
                .requestMatchers("/api/auth/**").authenticated()
                // 所有业务接口需要登录
                .requestMatchers("/api/**").authenticated()
                // 其他请求放行
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager，供AuthController登录使用
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
