package com.buu.oa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT工具类
 * 负责生成、解析、校验JWT Token
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;
    private final long expireTime;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-time}") long expireTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireTime = expireTime;
    }

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param roleCodes 角色编码列表
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, List<String> roleCodes) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roleCodes)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从Token中解析Claims
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验Token是否有效
     * @param token JWT Token
     * @return true有效 false无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("无效的JWT签名或格式: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取角色编码列表
     * @param token JWT Token
     * @return 角色编码列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoleCodes(String token) {
        return parseToken(token).get("roles", List.class);
    }
}
