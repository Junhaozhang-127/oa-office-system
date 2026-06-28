package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色Mapper
 * 角色查询、用户角色关联
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectByUserId(@Param("userId") Long userId);
}
