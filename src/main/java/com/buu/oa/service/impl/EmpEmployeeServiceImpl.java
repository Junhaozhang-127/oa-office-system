package com.buu.oa.service.impl;

import com.buu.oa.entity.AttendanceCheckin;
import com.buu.oa.mapper.AttendanceCheckinMapper;
import com.buu.oa.mapper.EmpEmployeeMapper;
import com.buu.oa.service.EmpEmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 员工档案Service实现
 * 整合员工基本信息与考勤统计数据
 */
@Service
public class EmpEmployeeServiceImpl implements EmpEmployeeService {

    private final EmpEmployeeMapper empEmployeeMapper;
    private final AttendanceCheckinMapper attendanceCheckinMapper;

    public EmpEmployeeServiceImpl(EmpEmployeeMapper empEmployeeMapper,
                                   AttendanceCheckinMapper attendanceCheckinMapper) {
        this.empEmployeeMapper = empEmployeeMapper;
        this.attendanceCheckinMapper = attendanceCheckinMapper;
    }

    @Override
    public Map<String, Object> getEmployeeList() {
        List<Map<String, Object>> rows = empEmployeeMapper.selectEmployeeList();
        Long total = empEmployeeMapper.selectEmployeeCount();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("rows", rows);
        return result;
    }

    @Override
    public Map<String, Object> getEmployeeDetail(Long empId) {
        Map<String, Object> employee = empEmployeeMapper.selectEmployeeDetail(empId);
        if (employee == null) {
            return null;
        }

        // 获取当月考勤统计
        YearMonth now = YearMonth.now();
        LocalDate startDate = now.atDay(1);
        LocalDate endDate = now.plusMonths(1).atDay(1);
        Map<String, Object> attendanceStats = attendanceCheckinMapper
                .selectMonthStats(empId, startDate, endDate);
        if (attendanceStats == null) {
            attendanceStats = new HashMap<>();
            attendanceStats.put("total_records", 0L);
            attendanceStats.put("normal_days", 0L);
            attendanceStats.put("late_days", 0L);
            attendanceStats.put("missing_days", 0L);
            attendanceStats.put("leave_days", 0L);
            attendanceStats.put("attendance_days", 0L);
        }

        // 获取最近10条打卡记录
        List<AttendanceCheckin> recentRecords = attendanceCheckinMapper
                .selectRecentRecords(empId, 10);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("employee", employee);
        result.put("attendanceStats", attendanceStats);
        result.put("recentRecords", recentRecords);
        return result;
    }
}
