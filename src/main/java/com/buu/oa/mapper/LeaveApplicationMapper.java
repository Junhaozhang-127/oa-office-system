package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.LeaveApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 请假申请Mapper
 * BaseMapper提供基础CRUD，自定义查询走XML
 */
public interface LeaveApplicationMapper extends BaseMapper<LeaveApplication> {

    /**
     * 查询当前员工请假申请列表
     * @param empId 员工ID
     * @return 申请记录列表
     */
    List<Map<String, Object>> selectByEmpId(@Param("empId") Long empId);
}
