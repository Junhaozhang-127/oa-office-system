package com.buu.oa.mapper;

import com.buu.oa.vo.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据看板统计Mapper
 * 所有统计SQL基于真实业务表关联查询，不写死假数据
 */
public interface DashboardMapper {

    /**
     * 统计员工总数
     */
    Long selectEmployeeCount();

    /**
     * 统计启用部门数量
     */
    Long selectDepartmentCount();

    /**
     * 统计今日考勤异常数量（迟到、缺卡，不含请假）
     */
    Long selectTodayAnomalyCount(@Param("today") LocalDate today);

    /**
     * 统计待审批申请总数（请假+加班+报销）
     */
    Long selectPendingApprovalCount();

    /**
     * 统计本月请假申请数量
     */
    Long selectMonthLeaveCount(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    /**
     * 统计本月加班申请数量
     */
    Long selectMonthOvertimeCount(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    /**
     * 统计本月报销总金额（仅统计已通过的报销单）
     */
    java.math.BigDecimal selectMonthExpenseAmount(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 统计未读公告数量
     */
    Long selectUnreadNoticeCount();

    /**
     * 统计各部门员工数量（含零员工的部门）
     */
    List<DepartmentEmployeeCountVO> selectDepartmentEmployeeCount();

    /**
     * 统计各请假类型申请数量
     */
    List<LeaveTypeStatisticsVO> selectLeaveTypeStatistics();

    /**
     * 统计报销趋势（按日期汇总）
     */
    List<ReimbursementTrendVO> selectReimbursementTrend(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 统计考勤异常率
     */
    AttendanceAnomalyRateVO selectAttendanceAnomalyRate(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /* ===== 导出查询 ===== */

    /** 导出：员工信息列表 */
    List<Map<String, Object>> selectEmployeeExportList();

    /** 导出：考勤记录 */
    List<Map<String, Object>> selectAttendanceExportList(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /** 导出：请假申请 */
    List<Map<String, Object>> selectLeaveExportList(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /** 导出：加班申请 */
    List<Map<String, Object>> selectOvertimeExportList(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /** 导出：报销记录 */
    List<Map<String, Object>> selectExpenseExportList(@Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /** 导出：审批记录 */
    List<Map<String, Object>> selectApprovalExportList();
}
