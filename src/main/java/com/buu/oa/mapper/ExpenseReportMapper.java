package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.ExpenseReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 报销单Mapper
 */
public interface ExpenseReportMapper extends BaseMapper<ExpenseReport> {

    /**
     * 查询当日最大报销单序号，用于生成报销单号
     * @param datePrefix 日期前缀（yyyyMMdd）
     * @return 当日最大序号
     */
    int selectMaxSeqByDate(@Param("datePrefix") String datePrefix);

    /**
     * 按员工ID查询报销单列表
     * @param empId 员工ID
     * @return 报销单列表
     */
    List<ExpenseReport> selectByEmpId(@Param("empId") Long empId);

    /**
     * 按状态查询报销单列表（联员工姓名）
     * @param status 审批状态
     * @return 报销单列表（含员工姓名、部门）
     */
    List<Map<String, Object>> selectWithEmployee(@Param("status") String status);
}
