package com.buu.oa.service;

import java.util.Map;

/**
 * 请假申请Service接口
 */
public interface LeaveService {

    /**
     * 提交请假申请
     * @param params 包含empId, leaveType, startDate, endDate, reason的请求参数
     * @return 提交结果
     */
    Map<String, Object> submitLeaveRequest(Map<String, Object> params);
}
