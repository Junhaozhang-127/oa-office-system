package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议室实体
 * 映射 meeting_room 表，记录会议室基础信息
 */
@Data
@TableName("meeting_room")
public class MeetingRoom {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 会议室名称 */
    private String roomName;
    /** 会议室编号 */
    private String roomCode;
    /** 容纳人数 */
    private Integer capacity;
    /** 位置 */
    private String location;
    /** 状态：1可用 0停用 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
