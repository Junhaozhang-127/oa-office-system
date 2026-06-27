package com.buu.oa.vo;

import java.math.BigDecimal;

/**
 * 报销趋势统计VO
 * 用于前端折线图展示报销金额趋势
 */
public class ReimbursementTrendVO {

    /** 日期 */
    private String date;
    /** 金额合计 */
    private BigDecimal amount;
    /** 报销单数 */
    private Long count;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
