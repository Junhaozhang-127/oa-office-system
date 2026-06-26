package com.buu.oa.service.impl;

import com.buu.oa.entity.AttendanceCheckin;
import com.buu.oa.mapper.AttendanceCheckinMapper;
import com.buu.oa.service.AttendanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 考勤日历Service实现
 * 将数据库打卡记录转换为日历网格结构，补充无记录日期的默认状态
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceCheckinMapper attendanceCheckinMapper;

    public AttendanceServiceImpl(AttendanceCheckinMapper attendanceCheckinMapper) {
        this.attendanceCheckinMapper = attendanceCheckinMapper;
    }

    @Override
    public Map<String, Object> getMonthCalendar(Long empId, Integer year, Integer month) {
        // 1. 查询当月所有打卡记录
        List<AttendanceCheckin> records = attendanceCheckinMapper.selectMonthCalendar(empId, year, month);

        // 2. 构建日期→打卡记录的映射，便于按日查找
        Map<LocalDate, AttendanceCheckin> recordMap = new LinkedHashMap<>();
        for (AttendanceCheckin r : records) {
            recordMap.put(r.getCheckDate(), r);
        }

        // 3. 生成当月完整日历网格（含空白补位）
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // 计算当月第一天是星期几（周一=1, 周日=7）
        int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue();

        List<Map<String, Object>> calendarDays = new ArrayList<>();

        // 月初空白占位格
        for (int i = 1; i < firstDayOfWeek; i++) {
            calendarDays.add(null);
        }

        // 当月每一天
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = yearMonth.atDay(d);
            Map<String, Object> dayInfo = new LinkedHashMap<>();
            dayInfo.put("day", d);
            dayInfo.put("date", date.toString());

            AttendanceCheckin record = recordMap.get(date);
            if (record != null) {
                dayInfo.put("status", record.getStatus());
                dayInfo.put("statusText", getStatusText(record.getStatus()));
                dayInfo.put("checkInTime", record.getCheckInTime());
                dayInfo.put("checkOutTime", record.getCheckOutTime());
            } else {
                // 周末标记为无记录（非工作日）
                int dow = date.getDayOfWeek().getValue();
                if (dow == 6 || dow == 7) {
                    dayInfo.put("status", 0);
                    dayInfo.put("statusText", "周末");
                } else {
                    dayInfo.put("status", 5);
                    dayInfo.put("statusText", "无记录");
                }
            }
            calendarDays.add(dayInfo);
        }

        // 4. 查询月度统计
        Map<String, Object> stats = attendanceCheckinMapper.selectMonthStats(empId, year, month);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total_records", 0L);
            stats.put("normal_days", 0L);
            stats.put("late_days", 0L);
            stats.put("missing_days", 0L);
            stats.put("leave_days", 0L);
            stats.put("attendance_days", 0L);
        }

        // 5. 组装返回数据
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("daysInMonth", daysInMonth);
        result.put("firstDayOfWeek", firstDayOfWeek);
        result.put("calendarDays", calendarDays);
        result.put("stats", stats);

        return result;
    }

    /**
     * 将打卡状态码转为中文描述
     * @param status 状态码：1正常 2迟到 3缺卡 4请假
     * @return 中文状态描述
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1: return "正常";
            case 2: return "迟到";
            case 3: return "缺卡";
            case 4: return "请假";
            default: return "未知";
        }
    }
}
