package com.buu.oa.service.impl;

import com.buu.oa.entity.MeetingReservation;
import com.buu.oa.mapper.MeetingReservationMapper;
import com.buu.oa.service.MeetingReservationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 会议预约Service实现
 * 核心功能：冲突检测、预约单号自动生成、预约管理
 */
@Service
public class MeetingReservationServiceImpl implements MeetingReservationService {

    private final MeetingReservationMapper meetingReservationMapper;

    public MeetingReservationServiceImpl(MeetingReservationMapper meetingReservationMapper) {
        this.meetingReservationMapper = meetingReservationMapper;
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
        return reservation;
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
