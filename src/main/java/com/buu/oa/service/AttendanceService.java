package com.buu.oa.service;

import java.util.Map;

/**
 * 考勤日历Service
 * 提供考勤日历数据查询与统计功能
 */
public interface AttendanceService {

    /**
     * 获取员工指定月份的考勤日历数据
     * 返回每一天的打卡状态列表，用于前端渲染日历网格
     * @param empId 员工ID
     * @param year  年份
     * @param month 月份
     * @return 日历数据（含每日状态和月度统计）
     */
    Map<String, Object> getMonthCalendar(Long empId, Integer year, Integer month);
}
