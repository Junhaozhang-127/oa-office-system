package com.buu.oa.service.impl;

import com.buu.oa.entity.MeetingReservation;
import com.buu.oa.mapper.MeetingReservationMapper;
import com.buu.oa.service.MeetingReservationService;
import com.buu.oa.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 会议预约Service实现
 * 核心功能：冲突检测、预约单号自动生成、预约管理、会议提醒（第七天集成）
 */
@Service
public class MeetingReservationServiceImpl implements MeetingReservationService {

    private static final Logger log = LoggerFactory.getLogger(MeetingReservationServiceImpl.class);

    private final MeetingReservationMapper meetingReservationMapper;
    private final ReminderService reminderService;

    public MeetingReservationServiceImpl(MeetingReservationMapper meetingReservationMapper,
                                         ReminderService reminderService) {
        this.meetingReservationMapper = meetingReservationMapper;
        this.reminderService = reminderService;
    }

    @Override
    public MeetingReservation createReservation(Long roomId, Long empId, String meetingTitle,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 String description) {
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("结束时间必须大于开始时间");
        }

        // 冲突检测：查询同一会议室在同一时间段内是否存在重叠预约
        int conflictCount = meetingReservationMapper.countConflict(roomId, startTime, endTime);
        if (conflictCount > 0) {
            throw new IllegalArgumentException("该会议室在所选时间段已被预约，请更换时间或会议室");
        }

        MeetingReservation reservation = new MeetingReservation();
        reservation.setReservationNo(generateReservationNo());
        reservation.setRoomId(roomId);
        reservation.setEmpId(empId);
        reservation.setMeetingTitle(meetingTitle);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setDescription(description != null ? description : "");
        reservation.setRemindStatus(0);
        reservation.setStatus(1);

        meetingReservationMapper.insert(reservation);

        // 会议预约成功后创建开始前15分钟提醒（第七天集成）
        scheduleMeetingReminder(reservation);

        return reservation;
    }

    /**
     * 为会议预约创建提醒任务
     * 提醒时间=会议开始时间-15分钟
     * 若提醒时间已过（距离开始不足15分钟），直接生成通知
     * @param reservation 预约记录
     */
    private void scheduleMeetingReminder(MeetingReservation reservation) {
        try {
            LocalDateTime remindTime = reservation.getStartTime().minusMinutes(15);
            long remindTimeMs = remindTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
            long nowMs = System.currentTimeMillis();

            String title = "会议提醒：" + reservation.getMeetingTitle();
            String content = "您预约的会议「" + reservation.getMeetingTitle()
                    + "」将于" + reservation.getStartTime().toString().replace("T", " ") + "开始，请准时参加。";

            if (remindTimeMs <= nowMs) {
                // 距离会议开始不足15分钟，直接生成通知而不入队
                log.info("会议{}距离开始不足15分钟，直接生成提醒通知", reservation.getReservationNo());
                reminderService.addReminderTask("MEETING", reservation.getId(), reservation.getEmpId(),
                        title, content, nowMs + 1000); // 1秒后立即消费
            } else {
                reminderService.addReminderTask("MEETING", reservation.getId(), reservation.getEmpId(),
                        title, content, remindTimeMs);
            }
        } catch (Exception e) {
            log.warn("创建会议提醒失败（非阻塞），预约单号={}，错误={}", reservation.getReservationNo(), e.getMessage());
        }
    }

    @Override
    public List<MeetingReservation> getMyReservations(Long empId) {
        return meetingReservationMapper.selectByEmpId(empId);
    }

    @Override
    public List<Map<String, Object>> getRoomReservations(Long roomId, String date) {
        return meetingReservationMapper.selectReservationsWithRoom(roomId, date);
    }

    @Override
    public boolean cancelReservation(Long id) {
        MeetingReservation reservation = meetingReservationMapper.selectById(id);
        if (reservation == null) {
            return false;
        }
        reservation.setStatus(0);
        return meetingReservationMapper.updateById(reservation) > 0;
    }

    /**
     * 生成预约单号：HY + yyyyMMdd + 3位序列号
     * 例如 HY20260627001
     * @return 预约单号
     */
    private String generateReservationNo() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int maxSeq = meetingReservationMapper.selectMaxSeqByDate(datePrefix);
        int nextSeq = maxSeq + 1;
        return "HY" + datePrefix + String.format("%03d", nextSeq);
    }
}
