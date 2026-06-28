package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报销明细实体
 * 映射 expense_report_item 表
 */
@Data
@TableName("expense_report_item")
public class ExpenseReportItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联报销单ID */
    private Long reportId;
    /** 费用项目 */
    private String itemName;
    /** 费用金额 */
    private BigDecimal amount;
    /** 发生日期 */
    private LocalDate expenseDate;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
}
