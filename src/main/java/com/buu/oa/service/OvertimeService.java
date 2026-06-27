package com.buu.oa.service;

import java.util.Map;

/**
 * 加班申请Service接口
 */
public interface OvertimeService {

    /**
     * 提交加班申请
     * @param params 包含empId, overtimeType, startTime, endTime, reason的请求参数
     * @return 提交结果
     */
    Map<String, Object> submitOvertimeRequest(Map<String, Object> params);
}
