package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.entity.MeetingReservation;
import com.buu.oa.security.SecurityUtils;
import com.buu.oa.service.MeetingReservationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 会议预约Controller
 * 提供会议预约的核心接口，含冲突检测，empId从Token获取
 */
@RestController
@RequestMapping("/api/meeting-reservation")
public class MeetingReservationController {

    private final MeetingReservationService meetingReservationService;

    public MeetingReservationController(MeetingReservationService meetingReservationService) {
        this.meetingReservationService = meetingReservationService;
    }

    /**
     * 创建会议预约（含冲突检测）
     * @param roomId      会议室ID
     * @param empId       预约人ID（前端兼容保留，实际从Token获取）
     * @param meetingTitle 会议主题
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param description 会议说明
     * @return 创建结果
     */
    @PostMapping("/create")
    public R<MeetingReservation> create(@RequestParam Long roomId,
                                         @RequestParam Long empId,
                                         @RequestParam String meetingTitle,
                                         @RequestParam String startTime,
                                         @RequestParam String endTime,
                                         @RequestParam(required = false, defaultValue = "") String description) {
        try {
            Long currentEmpId = SecurityUtils.getCurrentEmployeeId();
            if (currentEmpId != null) {
                empId = currentEmpId;
            }
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            MeetingReservation reservation = meetingReservationService.createReservation(
                    roomId, empId, meetingTitle, start, end, description);
            return R.success(reservation);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 查询我的会议列表
     * @param empId 员工ID（前端兼容保留，实际从Token获取）
     * @return 预约记录列表
     */
    @GetMapping("/my-list")
    public R<List<MeetingReservation>> myList(@RequestParam Long empId) {
        Long currentEmpId = SecurityUtils.getCurrentEmployeeId();
        if (currentEmpId != null) {
            empId = currentEmpId;
        }
        List<MeetingReservation> reservations = meetingReservationService.getMyReservations(empId);
        return R.success(reservations);
    }

    /**
     * 查询指定会议室某日的预约视图
     * @param roomId 会议室ID
     * @param date   日期（yyyy-MM-dd）
     * @return 当日预约列表
     */
    @GetMapping("/room-schedule")
    public R<List<Map<String, Object>>> roomSchedule(@RequestParam Long roomId,
                                                       @RequestParam String date) {
        List<Map<String, Object>> reservations = meetingReservationService.getRoomReservations(roomId, date);
        return R.success(reservations);
    }

    /**
     * 取消预约
     * @param id 预约ID
     * @return 取消结果
     */
    @PostMapping("/cancel")
    public R<Map<String, Object>> cancel(@RequestParam Long id) {
        boolean success = meetingReservationService.cancelReservation(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        if (success) {
            return R.success(result);
        }
        return R.fail("取消失败，预约记录不存在");
    }
}
