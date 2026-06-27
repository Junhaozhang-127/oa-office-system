package com.buu.oa.service.impl;

import com.buu.oa.enums.LeaveType;
import com.buu.oa.mapper.DashboardMapper;
import com.buu.oa.service.DashboardService;
import com.buu.oa.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板统计Service实现
 * 所有统计数据来自真实业务表，不做硬编码
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public DashboardSummaryVO getSummary() {
        DashboardSummaryVO vo = new DashboardSummaryVO();
        vo.setTotalEmployees(dashboardMapper.selectEmployeeCount());
        vo.setTotalDepartments(dashboardMapper.selectDepartmentCount());
        vo.setTodayAnomalies(dashboardMapper.selectTodayAnomalyCount(LocalDate.now()));

        Long pending = dashboardMapper.selectPendingApprovalCount();
        vo.setPendingApprovals(pending != null ? pending : 0L);

        // 本月范围
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1);

        vo.setMonthLeaveCount(dashboardMapper.selectMonthLeaveCount(monthStart, monthEnd));
        vo.setMonthOvertimeCount(dashboardMapper.selectMonthOvertimeCount(monthStart, monthEnd));

        BigDecimal expenseAmount = dashboardMapper.selectMonthExpenseAmount(monthStart, monthEnd);
        vo.setMonthExpenseAmount(expenseAmount != null ? expenseAmount : BigDecimal.ZERO);

        vo.setUnreadNotices(dashboardMapper.selectUnreadNoticeCount());

        log.info("首页统计汇总加载完成");
        return vo;
    }

    @Override
    public List<DepartmentEmployeeCountVO> getDepartmentEmployeeCount() {
        return dashboardMapper.selectDepartmentEmployeeCount();
    }

    @Override
    public List<LeaveTypeStatisticsVO> getLeaveTypeStatistics() {
        List<LeaveTypeStatisticsVO> list = dashboardMapper.selectLeaveTypeStatistics();
        // 补充类型中文名称
        for (LeaveTypeStatisticsVO vo : list) {
            LeaveType lt = LeaveType.fromCode(vo.getLeaveType());
            vo.setLeaveTypeName(lt != null ? lt.getLabel() : "未知类型(" + vo.getLeaveType() + ")");
        }
        return list;
    }

    @Override
    public List<ReimbursementTrendVO> getReimbursementTrend(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            // 默认最近30天
            endDate = LocalDate.now().plusDays(1);
            startDate = endDate.minusDays(31);
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        return dashboardMapper.selectReimbursementTrend(startDate, endDate);
    }

    @Override
    public AttendanceAnomalyRateVO getAttendanceAnomalyRate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            // 默认本月
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = startDate.plusMonths(1);
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        AttendanceAnomalyRateVO vo = dashboardMapper.selectAttendanceAnomalyRate(startDate, endDate);
        // 二次兜底：totalCount=0时避免除零
        if (vo != null && (vo.getTotalCount() == null || vo.getTotalCount() == 0)) {
            vo.setTotalCount(0L);
            vo.setAbnormalCount(0L);
            vo.setNormalCount(0L);
            vo.setAbnormalRate(BigDecimal.ZERO);
        }
        return vo;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getExportData(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            // 默认本月
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = startDate.plusMonths(1);
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put("employees", dashboardMapper.selectEmployeeExportList());
        result.put("attendances", dashboardMapper.selectAttendanceExportList(startDate, endDate));
        result.put("leaves", dashboardMapper.selectLeaveExportList(startDate, endDate));
        result.put("overtimes", dashboardMapper.selectOvertimeExportList(startDate, endDate));
        result.put("expenses", dashboardMapper.selectExpenseExportList(startDate, endDate));
        result.put("approvals", dashboardMapper.selectApprovalExportList());

        log.info("报表导出数据准备完成，日期范围：{} ~ {}", startDate, endDate);
        return result;
    }
}
