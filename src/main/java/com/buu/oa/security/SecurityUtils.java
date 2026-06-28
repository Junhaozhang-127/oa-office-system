package com.buu.oa.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 当前登录用户工具类
 * 从SecurityContext获取认证用户信息，替代开发阶段的CurrentUserHelper桩
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) auth.getPrincipal()).getUserId();
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     * @return 用户名，未登录返回null
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) auth.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * 获取当前登录用户关联的员工ID
     * @return 员工ID，未登录返回null
     */
    public static Long getCurrentEmployeeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) auth.getPrincipal()).getEmpId();
        }
        return null;
    }

    /**
     * 获取当前用户角色编码列表
     * @return 角色编码列表
     */
    public static List<String> getCurrentRoleCodes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * 判断当前用户是否拥有指定角色
     * @param roleCode 角色编码
     * @return true拥有 false不拥有
     */
    public static boolean hasRole(String roleCode) {
        return getCurrentRoleCodes().contains(roleCode);
    }

    /**
     * 判断当前用户是否为管理员
     * @return true是管理员
     */
    public static boolean isAdmin() {
        return hasRole("admin");
    }

    /**
     * 获取认证信息
     * @return Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
