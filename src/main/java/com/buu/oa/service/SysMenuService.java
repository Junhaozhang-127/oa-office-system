package com.buu.oa.service;

import com.buu.oa.entity.SysMenu;

import java.util.List;

/**
 * 菜单权限服务接口
 * 提供动态菜单树、按钮权限查询
 */
public interface SysMenuService {

    /**
     * 根据用户ID获取菜单树（仅type=0目录和type=1菜单，不含按钮）
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> getMenuListByUserId(Long userId);

    /**
     * 根据用户ID获取权限编码列表（type=2按钮权限）
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getPermsByUserId(Long userId);

    /**
     * 获取所有菜单（管理员）
     * @return 全部菜单列表
     */
    List<SysMenu> getAllMenus();
}
