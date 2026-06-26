package com.buu.oa.service;

import java.util.Map;

/**
 * 考勤日历Service
 * 日历网格与月度统计相互独立，分别加载互不影响
 */
public interface AttendanceService {

    /**
     * 获取考勤日历网格数据（仅日历，不含统计）
     * @param empId 员工ID
     * @param year  年份
     * @param month 月份
     * @return 日历网格数据（年、月、天数、首日星期、每日状态列表）
     */
    Map<String, Object> getCalendarGrid(Long empId, Integer year, Integer month);

    /**
     * 获取月度考勤统计（仅统计数据，独立于日历）
     * @param empId 员工ID
     * @param year  年份
     * @param month 月份
     * @return 月度统计数据（总记录、正常/迟到/缺卡/请假天数、出勤天数）
     */
    Map<String, Object> getMonthStats(Long empId, Integer year, Integer month);
}
