package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.service.ApprovalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批流Controller
 * 提供审批列表查询、审批操作（通过/驳回）、时间轴查询接口
 */
@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * 查询审批时间轴
     * @param businessType 业务类型
     * @param businessId   业务单据ID
     * @return 审批记录列表
     */
    @GetMapping("/timeline")
    public R<List<Map<String, Object>>> timeline(@RequestParam String businessType,
                                                  @RequestParam Long businessId) {
        return R.success(approvalService.getTimeline(businessType, businessId));
    }

    /**
     * 审批操作（通过或驳回）
     * 请求体：{ businessType, businessId, approverId, result(1通过/2驳回), opinion }
     */
    @PostMapping("/execute")
    public R<Map<String, Object>> execute(@RequestBody Map<String, Object> body) {
        try {
            String businessType = (String) body.get("businessType");
            Long businessId = Long.valueOf(body.get("businessId").toString());
            Long approverId = Long.valueOf(body.get("approverId").toString());
            Integer result = Integer.valueOf(body.get("result").toString());
            String opinion = (String) body.getOrDefault("opinion", "");

            Map<String, Object> res = approvalService.approve(
                    businessType, businessId, approverId, result, opinion);
            return R.success(res);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            return R.fail("审批操作失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的待审批列表
     * @param userId 用户ID（临时，后续从JWT获取）
     * @return 待审批单据列表
     */
    @GetMapping("/pending-list")
    public R<List<Map<String, Object>>> pendingList(@RequestParam Long userId) {
        return R.success(approvalService.getApprovalList(userId));
    }

    /**
     * 获取用户角色
     * @param userId 用户ID
     * @return 角色编码列表
     */
    @GetMapping("/user-roles")
    public R<List<String>> userRoles(@RequestParam Long userId) {
        return R.success(approvalService.getUserRoles(userId));
    }
}
