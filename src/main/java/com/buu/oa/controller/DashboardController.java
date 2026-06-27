package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.service.DashboardService;
import com.buu.oa.vo.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据看板Controller
 * 提供首页统计卡片、图表数据接口，按日期范围筛选
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 首页统计卡片汇总
     * @return 员工总数、部门数、待审批数、异常数等核心指标
     */
    @GetMapping("/summary")
    public R<DashboardSummaryVO> getSummary() {
        return R.success(dashboardService.getSummary());
    }

    /**
     * 各部门员工数量统计（柱状图）
     * @return 部门-人数列表
     */
    @GetMapping("/department-count")
    public R<List<DepartmentEmployeeCountVO>> getDepartmentEmployeeCount() {
        return R.success(dashboardService.getDepartmentEmployeeCount());
    }

    /**
     * 请假类型统计（饼图）
     * @return 请假类型-数量列表
     */
    @GetMapping("/leave-type-stats")
    public R<List<LeaveTypeStatisticsVO>> getLeaveTypeStatistics() {
        return R.success(dashboardService.getLeaveTypeStatistics());
    }

    /**
     * 报销趋势统计（折线图）
     * @param startDate 开始日期（可选，默认30天前）
     * @param endDate   结束日期（可选，默认今天）
     * @return 日期-金额-单数列表
     */
    @GetMapping("/reimbursement-trend")
    public R<List<ReimbursementTrendVO>> getReimbursementTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        try {
            return R.success(dashboardService.getReimbursementTrend(start, end));
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 考勤异常率统计（仪表盘）
     * @param startDate 开始日期（可选，默认本月1日）
     * @param endDate   结束日期（可选，默认下月1日）
     * @return 异常率、正常数、异常数、总数
     */
    @GetMapping("/attendance-anomaly")
    public R<AttendanceAnomalyRateVO> getAttendanceAnomalyRate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        try {
            return R.success(dashboardService.getAttendanceAnomalyRate(start, end));
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }
}
