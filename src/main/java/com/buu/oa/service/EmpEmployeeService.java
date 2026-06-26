package com.buu.oa.service;

import java.util.Map;

/**
 * 员工档案Service
 * 提供员工列表查询和详细信息获取
 */
public interface EmpEmployeeService {

    /**
     * 获取员工列表（含总数）
     * @return {total, rows}
     */
    Map<String, Object> getEmployeeList();

    /**
     * 获取员工详细信息（含本月考勤统计和近期打卡记录）
     * @param empId 员工ID
     * @return 员工详情Map，不存在时返回null
     */
    Map<String, Object> getEmployeeDetail(Long empId);
}
