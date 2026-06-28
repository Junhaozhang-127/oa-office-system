package com.buu.oa.service.impl;

import com.buu.oa.entity.ApprovalRecord;
import com.buu.oa.entity.ExpenseReport;
import com.buu.oa.entity.LeaveApplication;
import com.buu.oa.entity.OvertimeApplication;
import com.buu.oa.mapper.ApprovalRecordMapper;
import com.buu.oa.mapper.ExpenseReportMapper;
import com.buu.oa.mapper.LeaveApplicationMapper;
import com.buu.oa.mapper.OvertimeApplicationMapper;
import com.buu.oa.service.ApprovalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批流Service实现
 * 多级审批状态机：PENDING → MANAGER_APPROVED → FINANCE_APPROVED/COMPLETED
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRecordMapper approvalRecordMapper;
    private final ExpenseReportMapper expenseReportMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final OvertimeApplicationMapper overtimeApplicationMapper;

    public ApprovalServiceImpl(ApprovalRecordMapper approvalRecordMapper,
                                ExpenseReportMapper expenseReportMapper,
                                LeaveApplicationMapper leaveApplicationMapper,
                                OvertimeApplicationMapper overtimeApplicationMapper) {
        this.approvalRecordMapper = approvalRecordMapper;
        this.expenseReportMapper = expenseReportMapper;
        this.leaveApplicationMapper = leaveApplicationMapper;
        this.overtimeApplicationMapper = overtimeApplicationMapper;
    }

    @Override
    public List<Map<String, Object>> getTimeline(String businessType, Long businessId) {
        return approvalRecordMapper.selectTimeline(businessType, businessId);
    }

    @Override
    @Transactional
    public Map<String, Object> approve(String businessType, Long businessId, Long approverId,
                                        Integer result, String opinion) {
        // 1. 获取当前单据状态
        String currentStatus = getCurrentStatus(businessType, businessId);
        if (currentStatus == null) {
            throw new IllegalArgumentException("单据不存在");
        }

        // 2. 获取审批人角色
        List<String> roles = getUserRoles(approverId);
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("当前用户无审批权限");
        }
        String role = roles.contains("admin") ? "admin"
                : roles.contains("finance") ? "finance"
                : roles.contains("manager") ? "manager"
                : roles.get(0);

        // 3. 判断是驳回还是通过
        if (result == 2) {
            // 驳回：任意状态均可驳回
            updateBusinessStatus(businessType, businessId, "REJECTED");
            saveApprovalRecord(businessType, businessId, approverId, 2, opinion);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("success", true);
            map.put("newStatus", "REJECTED");
            map.put("message", "已驳回");
            return map;
        }

        // 4. 通过：状态机流转
        String newStatus = computeNextStatus(businessType, currentStatus, role);
        if (newStatus == null) {
            throw new IllegalArgumentException("当前状态不允许审批或角色权限不足");
        }

        updateBusinessStatus(businessType, businessId, newStatus);
        saveApprovalRecord(businessType, businessId, approverId, 1, opinion);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", true);
        map.put("newStatus", newStatus);
        map.put("message", "审批通过");
        return map;
    }

    @Override
    public List<Map<String, Object>> getApprovalList(Long userId) {
        List<String> roles = getUserRoles(userId);
        String role = roles.isEmpty() ? ""
                : roles.contains("admin") ? "admin"
                : roles.contains("finance") ? "finance"
                : roles.contains("manager") ? "manager"
                : roles.get(0);
        return approvalRecordMapper.selectApprovalList(role, null);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return approvalRecordMapper.selectRoleCodesByUserId(userId);
    }

    /**
     * 状态机：根据业务类型、当前状态、审批人角色计算下一状态
     */
    private String computeNextStatus(String businessType, String currentStatus, String role) {
        if ("EXPENSE".equals(businessType)) {
            if ("PENDING".equals(currentStatus) && ("manager".equals(role) || "admin".equals(role))) {
                return "MANAGER_APPROVED";
            }
            if ("MANAGER_APPROVED".equals(currentStatus) && ("finance".equals(role) || "admin".equals(role))) {
                return "COMPLETED";
            }
        }
        if ("LEAVE".equals(businessType) || "OVERTIME".equals(businessType)) {
            if ("PENDING".equals(currentStatus) && ("manager".equals(role) || "admin".equals(role))) {
                return "COMPLETED";
            }
        }
        // admin可以跨级操作
        if ("admin".equals(role)) {
            if ("PENDING".equals(currentStatus)) return "COMPLETED";
        }
        return null;
    }

    /**
     * 查询当前业务单据状态
     */
    private String getCurrentStatus(String businessType, Long businessId) {
        return switch (businessType) {
            case "EXPENSE" -> {
                ExpenseReport r = expenseReportMapper.selectById(businessId);
                yield r != null ? r.getStatus() : null;
            }
            case "LEAVE" -> {
                LeaveApplication l = leaveApplicationMapper.selectById(businessId);
                yield l != null ? l.getStatus() : null;
            }
            case "OVERTIME" -> {
                OvertimeApplication o = overtimeApplicationMapper.selectById(businessId);
                yield o != null ? o.getStatus() : null;
            }
            default -> null;
        };
    }

    /**
     * 更新业务单据状态
     */
    private void updateBusinessStatus(String businessType, Long businessId, String newStatus) {
        switch (businessType) {
            case "EXPENSE" -> {
                ExpenseReport r = expenseReportMapper.selectById(businessId);
                if (r != null) { r.setStatus(newStatus); expenseReportMapper.updateById(r); }
            }
            case "LEAVE" -> {
                LeaveApplication l = leaveApplicationMapper.selectById(businessId);
                if (l != null) { l.setStatus(newStatus); leaveApplicationMapper.updateById(l); }
            }
            case "OVERTIME" -> {
                OvertimeApplication o = overtimeApplicationMapper.selectById(businessId);
                if (o != null) { o.setStatus(newStatus); overtimeApplicationMapper.updateById(o); }
            }
        }
    }

    /**
     * 保存审批记录
     */
    private void saveApprovalRecord(String businessType, Long businessId, Long approverId,
                                     Integer result, String opinion) {
        ApprovalRecord record = new ApprovalRecord();
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setApproverId(approverId);
        record.setApprovalResult(result);
        record.setApprovalOpinion(opinion != null ? opinion : "");
        record.setApprovalTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }
}
