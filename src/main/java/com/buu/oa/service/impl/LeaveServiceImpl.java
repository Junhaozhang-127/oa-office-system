package com.buu.oa.service.impl;

import com.buu.oa.entity.LeaveApplication;
import com.buu.oa.enums.LeaveType;
import com.buu.oa.enums.ProcessStatus;
import com.buu.oa.mapper.LeaveApplicationMapper;
import com.buu.oa.service.LeaveService;
import com.buu.oa.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 包含参数校验、天数自动计算、单号生成、状态置为PENDING、审批通知（第七天集成）
 */
@Service
public class LeaveServiceImpl implements LeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveServiceImpl.class);

    private final LeaveApplicationMapper leaveApplicationMapper;
    private final NotificationService notificationService;

    public LeaveServiceImpl(LeaveApplicationMapper leaveApplicationMapper,
                            NotificationService notificationService) {
        this.leaveApplicationMapper = leaveApplicationMapper;
        this.notificationService = notificationService;
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

        // 请假提交后通知审批人（第七天集成）
        // TODO: 第八天RBAC接入后，根据部门/角色查询实际审批人替换此硬编码
        try {
            Long approverId = 3L; // 临时：默认通知部门经理（user_id=3，sys_user中lisi为经理角色）
            String title = "请假审批待办";
            String content = leaveType.getLabel() + "申请（单号：" + leaveNo + "），申请人ID：" + empId
                    + "，日期：" + startDate + "至" + endDate + "，共" + days + "天。";
            notificationService.createNotification("LEAVE", entity.getId(), approverId, title, content);
            log.info("请假审批通知已发送，单号={}，审批人ID={}", leaveNo, approverId);
        } catch (Exception e) {
            log.warn("发送请假审批通知失败（非阻塞），单号={}，错误={}", leaveNo, e.getMessage());
        }

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
