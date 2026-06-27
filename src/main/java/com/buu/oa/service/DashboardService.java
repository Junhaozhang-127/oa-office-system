package com.buu.oa.service;

import com.buu.oa.vo.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据看板统计Service接口
 * 提供首页汇总、图表数据、报表导出等统计查询
 */
public interface DashboardService {

    /**
     * 获取首页统计卡片汇总数据
     * @return 包含员工总数、部门数、待审批数、异常数等
     */
    DashboardSummaryVO getSummary();

    /**
     * 获取各部门员工数量统计（用于柱状图）
     * @return 部门-人数列表
     */
    List<DepartmentEmployeeCountVO> getDepartmentEmployeeCount();

    /**
     * 获取请假类型统计（用于饼图）
     * @return 请假类型-数量列表
     */
    List<LeaveTypeStatisticsVO> getLeaveTypeStatistics();

    /**
     * 获取报销趋势统计（用于折线图）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日期-金额-单数列表
     */
    List<ReimbursementTrendVO> getReimbursementTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取考勤异常率统计（用于仪表盘）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 异常率统计数据
     */
    AttendanceAnomalyRateVO getAttendanceAnomalyRate(LocalDate startDate, LocalDate endDate);

    /**
     * 导出多Sheet报表数据
     * 按日期范围查询各业务模块数据，封装为统一Map结构
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return {employees, attendances, leaves, overtimes, expenses, approvals}
     */
    Map<String, List<Map<String, Object>>> getExportData(LocalDate startDate, LocalDate endDate);
}
