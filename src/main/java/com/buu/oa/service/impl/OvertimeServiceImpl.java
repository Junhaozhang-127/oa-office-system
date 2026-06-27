package com.buu.oa.service.impl;

import com.buu.oa.entity.OvertimeApplication;
import com.buu.oa.enums.OvertimeType;
import com.buu.oa.enums.ProcessStatus;
import com.buu.oa.mapper.OvertimeApplicationMapper;
import com.buu.oa.service.OvertimeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 加班申请Service实现
 * 包含参数校验、小时数自动计算、单号生成、状态置为PENDING
 */
@Service
public class OvertimeServiceImpl implements OvertimeService {

    private final OvertimeApplicationMapper overtimeApplicationMapper;

    public OvertimeServiceImpl(OvertimeApplicationMapper overtimeApplicationMapper) {
        this.overtimeApplicationMapper = overtimeApplicationMapper;
    }

    @Override
    public Map<String, Object> submitOvertimeRequest(Map<String, Object> params) {
        // 参数校验
        Long empId = getLongParam(params, "empId");
        if (empId == null) {
            throw new IllegalArgumentException("申请人ID不能为空");
        }
        Integer overtimeTypeCode = getIntParam(params, "overtimeType");
        if (overtimeTypeCode == null) {
            throw new IllegalArgumentException("加班类型不能为空");
        }
        OvertimeType overtimeType = OvertimeType.fromCode(overtimeTypeCode);
        if (overtimeType == null) {
            throw new IllegalArgumentException("无效的加班类型: " + overtimeTypeCode);
        }
        String startTimeStr = getStringParam(params, "startTime");
        String endTimeStr = getStringParam(params, "endTime");
        if (startTimeStr == null || startTimeStr.isBlank()) {
            throw new IllegalArgumentException("开始时间不能为空");
        }
        if (endTimeStr == null || endTimeStr.isBlank()) {
            throw new IllegalArgumentException("结束时间不能为空");
        }

        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }

        String reason = getStringParam(params, "reason");
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("加班原因不能为空");
        }

        // 计算加班小时数（保留1位小数）
        long minutes = Duration.between(startTime, endTime).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);

        // 生成加班单号：JB + yyyyMMddHHmmss + 随机两位
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String overtimeNo = "JB" + timestamp + String.format("%02d", (int) (Math.random() * 100));

        // 构建实体并插入
        OvertimeApplication entity = new OvertimeApplication();
        entity.setOvertimeNo(overtimeNo);
        entity.setEmpId(empId);
        entity.setOvertimeType(overtimeTypeCode);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setHours(hours);
        entity.setReason(reason.trim());
        entity.setStatus(ProcessStatus.PENDING.name());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        overtimeApplicationMapper.insert(entity);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", entity.getId());
        result.put("overtimeNo", overtimeNo);
        result.put("hours", hours);
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
