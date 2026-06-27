package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.OvertimeApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 加班申请Mapper
 * BaseMapper提供基础CRUD，自定义查询走XML
 */
public interface OvertimeApplicationMapper extends BaseMapper<OvertimeApplication> {

    /**
     * 查询当前员工加班申请列表
     * @param empId 员工ID
     * @return 申请记录列表
     */
    List<Map<String, Object>> selectByEmpId(@Param("empId") Long empId);
}
