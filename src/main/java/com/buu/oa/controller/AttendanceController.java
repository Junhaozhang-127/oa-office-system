package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考勤日历Controller
 * 处理考勤日历相关前端请求，包含日历数据查询和统计接口
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 获取员工考勤日历数据
     * 包含当月每日打卡状态和月度统计信息
     * @param empId 员工ID（必填）
     * @param year  年份（选填，默认当前年）
     * @param month 月份（选填，默认当前月）
     * @return 日历数据与月度统计
     */
    @GetMapping("/calendar")
    public R<Map<String, Object>> getCalendar(@RequestParam Long empId,
                                               @RequestParam(required = false) Integer year,
                                               @RequestParam(required = false) Integer month) {
        java.time.YearMonth now = java.time.YearMonth.now();
        if (year == null) {
            year = now.getYear();
        }
        if (month == null) {
            month = now.getMonthValue();
        }

        Map<String, Object> data = attendanceService.getMonthCalendar(empId, year, month);
        return R.success(data);
    }
}
