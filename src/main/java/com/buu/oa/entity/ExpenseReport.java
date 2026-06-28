package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销单实体
 * 映射 expense_report 表
 */
@Data
@TableName("expense_report")
public class ExpenseReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 报销单号 */
    private String reportNo;
    /** 报销人ID */
    private Long empId;
    /** 报销总金额 */
    private BigDecimal totalAmount;
    /** 报销类型 */
    private String expenseType;
    /** 发票图片地址 */
    private String invoiceUrl;
    /** 报销说明 */
    private String description;
    /** 审批状态：PENDING/MANAGER_APPROVED/FINANCE_APPROVED/REJECTED/COMPLETED */
    private String status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
