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
 * 日历网格与月度统计相互独立，各自调用互不影响
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceCheckinMapper attendanceCheckinMapper;

    public AttendanceServiceImpl(AttendanceCheckinMapper attendanceCheckinMapper) {
        this.attendanceCheckinMapper = attendanceCheckinMapper;
    }

    @Override
    public Map<String, Object> getCalendarGrid(Long empId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1);
        List<AttendanceCheckin> records = attendanceCheckinMapper.selectMonthCalendar(empId, startDate, endDate);

        Map<LocalDate, AttendanceCheckin> recordMap = new LinkedHashMap<>();
        for (AttendanceCheckin r : records) {
            recordMap.put(r.getCheckDate(), r);
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue();

        List<Map<String, Object>> calendarDays = new ArrayList<>();

        for (int i = 1; i < firstDayOfWeek; i++) {
            calendarDays.add(null);
        }

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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("daysInMonth", daysInMonth);
        result.put("firstDayOfWeek", firstDayOfWeek);
        result.put("calendarDays", calendarDays);
        return result;
    }

    @Override
    public Map<String, Object> getMonthStats(Long empId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1);
        Map<String, Object> stats = attendanceCheckinMapper.selectMonthStats(empId, startDate, endDate);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total_records", 0L);
            stats.put("normal_days", 0L);
            stats.put("late_days", 0L);
            stats.put("missing_days", 0L);
            stats.put("leave_days", 0L);
            stats.put("attendance_days", 0L);
        }
        return stats;
    }

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
