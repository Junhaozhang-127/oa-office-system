package com.buu.oa.service.impl;

import com.buu.oa.entity.LeaveApplication;
import com.buu.oa.enums.LeaveType;
import com.buu.oa.enums.ProcessStatus;
import com.buu.oa.mapper.LeaveApplicationMapper;
import com.buu.oa.service.LeaveService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 请假申请Service实现
 * 包含参数校验、天数自动计算、单号生成、状态置为PENDING
 */
@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveApplicationMapper leaveApplicationMapper;

    public LeaveServiceImpl(LeaveApplicationMapper leaveApplicationMapper) {
        this.leaveApplicationMapper = leaveApplicationMapper;
    }

    @Override
    public Map<String, Object> submitLeaveRequest(Map<String, Object> params) {
        // 参数校验
        Long empId = getLongParam(params, "empId");
        if (empId == null) {
            throw new IllegalArgumentException("申请人ID不能为空");
        }
        Integer leaveTypeCode = getIntParam(params, "leaveType");
        if (leaveTypeCode == null) {
            throw new IllegalArgumentException("请假类型不能为空");
        }
        LeaveType leaveType = LeaveType.fromCode(leaveTypeCode);
        if (leaveType == null) {
            throw new IllegalArgumentException("无效的请假类型: " + leaveTypeCode);
        }
        String startDateStr = getStringParam(params, "startDate");
        String endDateStr = getStringParam(params, "endDate");
        if (startDateStr == null || startDateStr.isBlank()) {
            throw new IllegalArgumentException("开始日期不能为空");
        }
        if (endDateStr == null || endDateStr.isBlank()) {
            throw new IllegalArgumentException("结束日期不能为空");
        }

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束日期必须晚于开始日期");
        }

        String reason = getStringParam(params, "reason");
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("申请原因不能为空");
        }

        // 计算请假天数（含头尾）
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal days = BigDecimal.valueOf(daysBetween);

        // 生成请假单号：QJ + yyyyMMddHHmmss + 随机两位
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String leaveNo = "QJ" + timestamp + String.format("%02d", (int) (Math.random() * 100));

        // 构建实体并插入
        LeaveApplication entity = new LeaveApplication();
        entity.setLeaveNo(leaveNo);
        entity.setEmpId(empId);
        entity.setLeaveType(leaveTypeCode);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setDays(days);
        entity.setReason(reason.trim());
        entity.setStatus(ProcessStatus.PENDING.name());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        leaveApplicationMapper.insert(entity);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", entity.getId());
        result.put("leaveNo", leaveNo);
        result.put("days", days);
        result.put("status", ProcessStatus.PENDING.name());
        result.put("statusText", ProcessStatus.PENDING.getLabel());
        return result;
    }

    private String getStringParam(Map<String, Object> params, String key) {
        Object val = params.get(key);
        return val != null ? val.toString() : null;
    }

    private Long getLongParam(Map<String, Object> params, String key) {
        Object val = params.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntParam(Map<String, Object> params, String key) {
        Object val = params.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
