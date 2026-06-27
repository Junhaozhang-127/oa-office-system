package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体
 * 映射leave_application表
 */
@Data
@TableName("leave_application")
public class LeaveApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String leaveNo;
    private Long empId;
    private Integer leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal days;
    private String reason;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
