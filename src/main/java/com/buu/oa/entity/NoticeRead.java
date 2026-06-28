package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告已读记录实体
 * 映射sys_notice_read表
 */
@Data
@TableName("sys_notice_read")
public class NoticeRead {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noticeId;
    private Long userId;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
