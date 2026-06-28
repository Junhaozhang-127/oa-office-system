package com.buu.oa.service.impl;

import com.buu.oa.entity.SysMenu;
import com.buu.oa.mapper.SysMenuMapper;
import com.buu.oa.mapper.SysUserMapper;
import com.buu.oa.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单权限服务实现
 * 根据用户角色加载动态菜单和按钮权限
 */
@Service
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysUserMapper sysUserMapper;

    public SysMenuServiceImpl(SysMenuMapper sysMenuMapper, SysUserMapper sysUserMapper) {
        this.sysMenuMapper = sysMenuMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<SysMenu> getMenuListByUserId(Long userId) {
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);
        List<SysMenu> menus;
        if (roleCodes.contains("admin")) {
            menus = sysMenuMapper.selectAllMenus();
        } else {
            menus = sysMenuMapper.selectByUserId(userId);
        }
        // 只返回目录和菜单（type=0,1），过滤按钮权限
        return menus.stream()
                .filter(m -> m.getType() != null && m.getType() != 2)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPermsByUserId(Long userId) {
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);
        if (roleCodes.contains("admin")) {
            // 管理员返回所有按钮权限
            List<SysMenu> allMenus = sysMenuMapper.selectAllMenus();
            return allMenus.stream()
                    .filter(m -> m.getType() != null && m.getType() == 2 && m.getPerms() != null && !m.getPerms().isEmpty())
                    .map(SysMenu::getPerms)
                    .collect(Collectors.toList());
        }
        return sysMenuMapper.selectPermsByUserId(userId);
    }

    @Override
    public List<SysMenu> getAllMenus() {
        return sysMenuMapper.selectAllMenus();
    }
}
