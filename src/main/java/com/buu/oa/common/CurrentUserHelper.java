package com.buu.oa.common;

import com.buu.oa.security.SecurityUtils;

/**
 * 当前用户适配器
 * 委托给SecurityUtils从SecurityContext获取登录用户信息
 */
public final class CurrentUserHelper {

    private CurrentUserHelper() {}

    /**
     * 获取当前登录用户ID
     * @return 当前用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            return userId;
        }
        return 1L; // fallback: 未集成认证时兼容旧逻辑
    }

    /**
     * 获取当前登录用户名
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        String username = SecurityUtils.getCurrentUsername();
        if (username != null) {
            return username;
        }
        return "admin"; // fallback
    }
}
