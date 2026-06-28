package com.buu.oa.service;

import java.util.List;
import java.util.Map;

/**
 * 审批流Service
 * 多级审批状态机 + 角色权限隔离
 */
public interface ApprovalService {

    /**
     * 查询审批时间轴
     * @param businessType 业务类型：EXPENSE/LEAVE/OVERTIME
     * @param businessId   业务单据ID
     * @return 审批记录列表（含审批人姓名）
     */
    List<Map<String, Object>> getTimeline(String businessType, Long businessId);

    /**
     * 审批操作（通过或驳回）
     * @param businessType 业务类型
     * @param businessId   业务单据ID
     * @param approverId   审批人ID
     * @param result       审批结果：1通过 2驳回
     * @param opinion      审批意见
     * @return 操作结果描述
     * @throws IllegalArgumentException 状态流转非法或权限不足
     */
    Map<String, Object> approve(String businessType, Long businessId, Long approverId,
                                Integer result, String opinion);

    /**
     * 按用户角色查询待审批列表
     * @param userId 用户ID
     * @return 待审批单据列表
     */
    List<Map<String, Object>> getApprovalList(Long userId);

    /**
     * 获取用户角色编码列表
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);
}
