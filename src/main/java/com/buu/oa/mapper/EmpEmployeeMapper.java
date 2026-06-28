package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.EmpEmployee;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 员工档案Mapper
 * 继承BaseMapper获得基础CRUD，自定义查询联查部门名称
 */
public interface EmpEmployeeMapper extends BaseMapper<EmpEmployee> {

    /**
     * 查询全部员工列表（含部门名称）
     * @return 员工列表，每行含deptName字段
     */
    List<Map<String, Object>> selectEmployeeList();

    /**
     * 查询单个员工详细信息（含部门名称）
     * @param empId 员工ID
     * @return 员工信息Map，含deptName字段
     */
    Map<String, Object> selectEmployeeDetail(@Param("empId") Long empId);

    /**
     * 查询员工总数
     * @return 员工总数
     */
    Long selectEmployeeCount();

    /**
     * 查询所有员工ID列表
     * @return 员工ID列表
     */
    List<Long> selectAllEmpIds();
}
