package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议预约实体
 * 映射 meeting_reservation 表，记录会议预约信息
 */
@Data
@TableName("meeting_reservation")
public class MeetingReservation {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 预约单号 */
    private String reservationNo;
    /** 会议室ID */
    private Long roomId;
    /** 预约人ID */
    private Long empId;
    /** 会议主题 */
    private String meetingTitle;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 会议说明 */
    private String description;
    /** 提醒状态：1已提醒 0未提醒 */
    private Integer remindStatus;
    /** 预约状态：1有效 0取消 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
