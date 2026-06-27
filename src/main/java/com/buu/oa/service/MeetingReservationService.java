package com.buu.oa.service;

import com.buu.oa.entity.MeetingReservation;

import java.util.List;
import java.util.Map;

/**
 * 会议预约Service
 * 提供会议预约的核心业务逻辑，含冲突检测
 */
public interface MeetingReservationService {

    /**
     * 创建会议预约（含冲突检测）
     * @param roomId       会议室ID
     * @param empId        预约人ID
     * @param meetingTitle 会议主题
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param description  会议说明
     * @return 创建成功的预约记录
     * @throws IllegalArgumentException 时间冲突时抛出
     */
    MeetingReservation createReservation(Long roomId, Long empId, String meetingTitle,
                                          java.time.LocalDateTime startTime, java.time.LocalDateTime endTime,
                                          String description);

    /**
     * 查询员工的全部预约记录
     * @param empId 员工ID
     * @return 预约记录列表
     */
    List<MeetingReservation> getMyReservations(Long empId);

    /**
     * 查询指定会议室某日的预约视图
     * @param roomId 会议室ID
     * @param date   日期（yyyy-MM-dd）
     * @return 当日预约列表（含会议室名称）
     */
    List<Map<String, Object>> getRoomReservations(Long roomId, String date);

    /**
     * 取消预约
     * @param id 预约ID
     * @return true成功 false失败
     */
    boolean cancelReservation(Long id);
}
