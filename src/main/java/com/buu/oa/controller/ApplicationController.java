package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 我的申请Controller
 * 统一查询请假和加班申请列表
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * 查询我的申请列表（请假+加班）
     * @param empId 员工ID
     * @return 统一申请列表，按创建时间倒序
     */
    @GetMapping("/my")
    public R<Map<String, Object>> getMyApplications(@RequestParam Long empId) {
        Map<String, Object> data = applicationService.getMyApplications(empId);
        return R.success(data);
    }
}
