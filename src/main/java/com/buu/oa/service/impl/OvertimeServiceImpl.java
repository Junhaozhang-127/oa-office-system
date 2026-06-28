package com.buu.oa.service.impl;

import com.buu.oa.entity.OvertimeApplication;
import com.buu.oa.enums.OvertimeType;
import com.buu.oa.enums.ProcessStatus;
import com.buu.oa.mapper.OvertimeApplicationMapper;
import com.buu.oa.service.NotificationService;
import com.buu.oa.service.OvertimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 包含参数校验、小时数自动计算、单号生成、状态置为PENDING、审批通知（第七天集成）
 */
@Service
public class OvertimeServiceImpl implements OvertimeService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeServiceImpl.class);

    private final OvertimeApplicationMapper overtimeApplicationMapper;
    private final NotificationService notificationService;

    public OvertimeServiceImpl(OvertimeApplicationMapper overtimeApplicationMapper,
                               NotificationService notificationService) {
        this.overtimeApplicationMapper = overtimeApplicationMapper;
        this.notificationService = notificationService;
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

        // 加班提交后通知审批人（第七天集成）
        // TODO: 第八天RBAC接入后，根据部门/角色查询实际审批人替换此硬编码
        try {
            Long approverId = 3L;
            String title = "加班审批待办";
            String content = overtimeType.getLabel() + "申请（单号：" + overtimeNo + "），申请人ID：" + empId
                    + "，时间：" + startTime + "至" + endTime + "，共" + hours + "小时。";
            notificationService.createNotification("OVERTIME", entity.getId(), approverId, title, content);
            log.info("加班审批通知已发送，单号={}，审批人ID={}", overtimeNo, approverId);
        } catch (Exception e) {
            log.warn("发送加班审批通知失败（非阻塞），单号={}，错误={}", overtimeNo, e.getMessage());
        }

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
