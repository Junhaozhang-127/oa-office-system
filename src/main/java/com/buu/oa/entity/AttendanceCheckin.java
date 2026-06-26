package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤打卡实体
 * 映射 attendance_checkin 表，记录员工每日打卡数据
 */
@Data
@TableName("attendance_checkin")
public class AttendanceCheckin {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 员工ID */
    private Long empId;
    /** 打卡日期 */
    private LocalDate checkDate;
    /** 上班打卡时间 */
    private LocalDateTime checkInTime;
    /** 下班打卡时间 */
    private LocalDateTime checkOutTime;
    /** 状态：1正常 2迟到 3缺卡 4请假 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
