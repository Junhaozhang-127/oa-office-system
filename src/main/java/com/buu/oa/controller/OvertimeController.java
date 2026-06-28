package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.security.SecurityUtils;
import com.buu.oa.service.OvertimeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 加班申请Controller
 * 处理加班提交等前端请求，empId从当前登录用户获取
 */
@RestController
@RequestMapping("/api/overtime-requests")
public class OvertimeController {

    private final OvertimeService overtimeService;

    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    /**
     * 提交加班申请
     * @param params 包含overtimeType, startTime, endTime, reason（empId从Token获取）
     * @return 提交结果，含单号和状态
     */
    @PostMapping
    public R<Map<String, Object>> submit(@RequestBody Map<String, Object> params) {
        try {
            params.put("empId", SecurityUtils.getCurrentEmployeeId());
            Map<String, Object> data = overtimeService.submitOvertimeRequest(params);
            return R.success(data);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }
}
