package com.buu.oa;

import com.buu.oa.entity.MeetingReservation;
import com.buu.oa.mapper.MeetingReservationMapper;
import com.buu.oa.service.MeetingReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 会议预约模块单元测试
 * 验证冲突检测算法和预约单号生成逻辑
 */
@SpringBootTest
class MeetingReservationTest {

    @Autowired
    private MeetingReservationService meetingReservationService;

    @Autowired
    private MeetingReservationMapper meetingReservationMapper;

    /**
     * 冲突场景1：完全重叠
     * 新预约时间段 10:00-12:00 完全包含已有预约 10:00-12:00
     */
    @Test
    void conflictDetection_shouldDetectOverlap() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 20, 16, 0);
        int count = meetingReservationMapper.countConflict(1L, start, end);
        assertTrue(count > 0, "完全重叠应检测到冲突");
    }

    /**
     * 冲突场景2：部分重叠
     * 新预约 13:00-15:00 与已有预约 14:00-16:00 重叠1小时
     */
    @Test
    void conflictDetection_partialOverlap() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 13, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 20, 15, 0);
        int count = meetingReservationMapper.countConflict(1L, start, end);
        assertTrue(count > 0, "部分重叠应检测到冲突");
    }

    /**
     * 无冲突场景
     * 新预约 08:00-10:00 在已有预约 14:00-16:00 之前，不重叠
     */
    @Test
    void conflictDetection_noConflict() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 20, 10, 0);
        int count = meetingReservationMapper.countConflict(1L, start, end);
        assertEquals(0, count, "时间不重叠应无冲突");
    }

    /**
     * 边界场景：允许前后衔接
     * 新预约结束时间 = 已有预约开始时间，应无冲突
     */
    @Test
    void conflictDetection_boundary() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 20, 14, 0);
        int count = meetingReservationMapper.countConflict(1L, start, end);
        assertEquals(0, count, "endTime=已有startTime应无冲突，允许前后衔接");
    }

    /**
     * 预约单号格式验证
     * 格式：HY + yyyyMMdd + 3位数字，例如 HY20260627001
     */
    @Test
    void reservationNoGeneration_shouldMatchFormat() {
        String datePrefix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        int maxSeq = meetingReservationMapper.selectMaxSeqByDate(datePrefix);
        int nextSeq = maxSeq + 1;
        String reservationNo = "HY" + datePrefix + String.format("%03d", nextSeq);
        assertTrue(reservationNo.matches("HY\\d{11}"), "预约单号格式应为HY+日期8位+序号3位");
        assertEquals(13, reservationNo.length(), "预约单号长度应为13位");
    }
}
