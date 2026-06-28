package com.buu.oa.common;

/**
 * 当前用户临时适配器
 * 当前项目尚未完成登录认证体系，
 * 提供统一的当前用户获取入口，第八天接入Spring Security + JWT时只需替换此实现
 * TODO: 第八天替换为 SecurityContextHolder.getContext().getAuthentication()
 */
public final class CurrentUserHelper {

    private CurrentUserHelper() {}

    /** 默认用户ID（开发阶段使用admin），后续从SecurityContext获取 */
    private static final Long DEFAULT_USER_ID = 1L;

    /**
     * 获取当前登录用户ID
     * @return 当前用户ID
     */
    public static Long getCurrentUserId() {
        // TODO: 第八天替换为:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // if (auth != null && auth.getPrincipal() instanceof UserDetails) {
        //     return ((UserDetails) auth.getPrincipal()).getId();
        // }
        return DEFAULT_USER_ID;
    }

    /**
     * 获取当前登录用户名
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        return "admin";
    }
}
