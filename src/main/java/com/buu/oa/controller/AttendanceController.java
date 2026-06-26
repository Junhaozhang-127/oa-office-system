package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考勤日历Controller
 * 日历网格与月度统计为两个独立接口，前端分开请求、互不影响
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 获取考勤日历网格（仅日历，不含统计）
     */
    @GetMapping("/calendar")
    public R<Map<String, Object>> getCalendar(@RequestParam Long empId,
                                               @RequestParam(required = false) Integer year,
                                               @RequestParam(required = false) Integer month) {
        java.time.YearMonth now = java.time.YearMonth.now();
        if (year == null) year = now.getYear();
        if (month == null) month = now.getMonthValue();

        Map<String, Object> data = attendanceService.getCalendarGrid(empId, year, month);
        return R.success(data);
    }

    /**
     * 获取月考勤统计（仅统计，独立于日历）
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats(@RequestParam Long empId,
                                            @RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer month) {
        java.time.YearMonth now = java.time.YearMonth.now();
        if (year == null) year = now.getYear();
        if (month == null) month = now.getMonthValue();

        Map<String, Object> data = attendanceService.getMonthStats(empId, year, month);
        return R.success(data);
    }
}
