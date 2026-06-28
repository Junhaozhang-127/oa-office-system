package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单权限Mapper
 * 动态菜单树、按钮权限、角色菜单关联查询
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询有权限的菜单列表（含按钮权限）
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID列表查询菜单
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    List<SysMenu> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 查询所有菜单（管理员用）
     * @return 全部菜单列表
     */
    List<SysMenu> selectAllMenus();

    /**
     * 根据用户ID查询权限编码列表
     * @param userId 用户ID
     * @return 权限编码列表（如 employee:list）
     */
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
