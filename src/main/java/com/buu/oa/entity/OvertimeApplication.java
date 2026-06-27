package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班申请实体
 * 映射overtime_application表
 */
@Data
@TableName("overtime_application")
public class OvertimeApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String overtimeNo;
    private Long empId;
    private Integer overtimeType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal hours;
    private String reason;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
