package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.security.SecurityUtils;
import com.buu.oa.service.LeaveService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 请假申请Controller
 * 处理请假提交等前端请求，empId从当前登录用户获取
 */
@RestController
@RequestMapping("/api/leave-requests")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    /**
     * 提交请假申请
     * @param params 包含leaveType, startDate, endDate, reason（empId从Token获取）
     * @return 提交结果，含单号和状态
     */
    @PostMapping
    public R<Map<String, Object>> submit(@RequestBody Map<String, Object> params) {
        try {
            params.put("empId", SecurityUtils.getCurrentEmployeeId());
            Map<String, Object> data = leaveService.submitLeaveRequest(params);
            return R.success(data);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }
}
