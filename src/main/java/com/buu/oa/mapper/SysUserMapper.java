package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户Mapper
 * 用户登录认证、角色查询
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户（含角色信息）
     * @param username 登录用户名
     * @return 用户实体
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询角色编码列表
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
