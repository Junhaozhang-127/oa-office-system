package com.buu.oa.vo;

import java.math.BigDecimal;

/**
 * 首页统计卡片汇总VO
 * 封装管理员驾驶舱顶部核心统计数据
 */
public class DashboardSummaryVO {

    /** 员工总数 */
    private Long totalEmployees;
    /** 部门总数 */
    private Long totalDepartments;
    /** 今日考勤异常数量 */
    private Long todayAnomalies;
    /** 待审批申请数量 */
    private Long pendingApprovals;
    /** 本月请假申请数量 */
    private Long monthLeaveCount;
    /** 本月加班申请数量 */
    private Long monthOvertimeCount;
    /** 本月报销总金额 */
    private BigDecimal monthExpenseAmount;
    /** 未读公告数量 */
    private Long unreadNotices;

    public Long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(Long totalEmployees) { this.totalEmployees = totalEmployees; }
    public Long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(Long totalDepartments) { this.totalDepartments = totalDepartments; }
    public Long getTodayAnomalies() { return todayAnomalies; }
    public void setTodayAnomalies(Long todayAnomalies) { this.todayAnomalies = todayAnomalies; }
    public Long getPendingApprovals() { return pendingApprovals; }
    public void setPendingApprovals(Long pendingApprovals) { this.pendingApprovals = pendingApprovals; }
    public Long getMonthLeaveCount() { return monthLeaveCount; }
    public void setMonthLeaveCount(Long monthLeaveCount) { this.monthLeaveCount = monthLeaveCount; }
    public Long getMonthOvertimeCount() { return monthOvertimeCount; }
    public void setMonthOvertimeCount(Long monthOvertimeCount) { this.monthOvertimeCount = monthOvertimeCount; }
    public BigDecimal getMonthExpenseAmount() { return monthExpenseAmount; }
    public void setMonthExpenseAmount(BigDecimal monthExpenseAmount) { this.monthExpenseAmount = monthExpenseAmount; }
    public Long getUnreadNotices() { return unreadNotices; }
    public void setUnreadNotices(Long unreadNotices) { this.unreadNotices = unreadNotices; }
}
