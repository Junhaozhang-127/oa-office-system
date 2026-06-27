package com.buu.oa.service;

import java.util.Map;

/**
 * 我的申请Service接口
 * 统一查询请假和加班申请
 */
public interface ApplicationService {

    /**
     * 查询我的申请列表（包含请假和加班）
     * @param empId 员工ID
     * @return 统一申请列表
     */
    Map<String, Object> getMyApplications(Long empId);
}
