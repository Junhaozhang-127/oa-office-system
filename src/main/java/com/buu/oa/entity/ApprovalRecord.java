package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批记录实体
 * 映射 approval_record 表
 */
@Data
@TableName("approval_record")
public class ApprovalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 业务类型：LEAVE/OVERTIME/EXPENSE */
    private String businessType;
    /** 业务单据ID */
    private Long businessId;
    /** 审批人ID */
    private Long approverId;
    /** 审批结果：1通过 2驳回 */
    private Integer approvalResult;
    /** 审批意见 */
    private String approvalOpinion;
    /** 审批时间 */
    private LocalDateTime approvalTime;
    /** 创建时间 */
    private LocalDateTime createTime;
}
