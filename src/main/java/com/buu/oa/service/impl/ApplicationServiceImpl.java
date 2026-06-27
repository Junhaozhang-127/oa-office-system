package com.buu.oa.service.impl;

import com.buu.oa.enums.LeaveType;
import com.buu.oa.enums.OvertimeType;
import com.buu.oa.enums.ProcessStatus;
import com.buu.oa.mapper.LeaveApplicationMapper;
import com.buu.oa.mapper.OvertimeApplicationMapper;
import com.buu.oa.service.ApplicationService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 我的申请Service实现
 * 合并请假和加班申请为统一列表，按创建时间倒序
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final LeaveApplicationMapper leaveApplicationMapper;
    private final OvertimeApplicationMapper overtimeApplicationMapper;

    public ApplicationServiceImpl(LeaveApplicationMapper leaveApplicationMapper,
                                   OvertimeApplicationMapper overtimeApplicationMapper) {
        this.leaveApplicationMapper = leaveApplicationMapper;
        this.overtimeApplicationMapper = overtimeApplicationMapper;
    }

    @Override
    public Map<String, Object> getMyApplications(Long empId) {
        // 分别查询请假和加班申请
        List<Map<String, Object>> leaveList = leaveApplicationMapper.selectByEmpId(empId);
        List<Map<String, Object>> overtimeList = overtimeApplicationMapper.selectByEmpId(empId);

        // 合并为统一列表
        List<Map<String, Object>> allList = new ArrayList<>();

        for (Map<String, Object> item : leaveList) {
            Map<String, Object> unified = new LinkedHashMap<>(item);
            unified.put("applicationType", "LEAVE");
            unified.put("applicationTypeText", "请假");

            // 转换leaveType为文本
            Object typeObj = item.get("leaveType");
            if (typeObj instanceof Number) {
                LeaveType lt = LeaveType.fromCode(((Number) typeObj).intValue());
                unified.put("leaveTypeText", lt != null ? lt.getLabel() : "未知");
            }

            // 时间范围：请假用startDate～endDate
            unified.put("timeRange", item.get("startDate") + " ~ " + item.get("endDate"));
            unified.put("amount", item.get("days"));
            unified.put("amountUnit", "天");

            // 状态文本
            Object statusObj = item.get("status");
            if (statusObj != null) {
                ProcessStatus ps = ProcessStatus.fromName(statusObj.toString());
                unified.put("statusText", ps != null ? ps.getLabel() : statusObj.toString());
            }
            allList.add(unified);
        }

        for (Map<String, Object> item : overtimeList) {
            Map<String, Object> unified = new LinkedHashMap<>(item);
            unified.put("applicationType", "OVERTIME");
            unified.put("applicationTypeText", "加班");

            // 转换overtimeType为文本
            Object typeObj = item.get("overtimeType");
            if (typeObj instanceof Number) {
                OvertimeType ot = OvertimeType.fromCode(((Number) typeObj).intValue());
                unified.put("overtimeTypeText", ot != null ? ot.getLabel() : "未知");
            }

            // 时间范围：加班用startTime～endTime
            unified.put("timeRange", item.get("startTime") + " ~ " + item.get("endTime"));
            unified.put("amount", item.get("hours"));
            unified.put("amountUnit", "小时");

            // 状态文本
            Object statusObj = item.get("status");
            if (statusObj != null) {
                ProcessStatus ps = ProcessStatus.fromName(statusObj.toString());
                unified.put("statusText", ps != null ? ps.getLabel() : statusObj.toString());
            }
            allList.add(unified);
        }

        // 按创建时间倒序
        allList.sort((a, b) -> {
            Object t1 = a.get("createTime");
            Object t2 = b.get("createTime");
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.toString().compareTo(t1.toString());
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", allList.size());
        result.put("rows", allList);
        return result;
    }
}
