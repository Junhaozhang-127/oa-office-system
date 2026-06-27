package com.buu.oa.vo;

import java.math.BigDecimal;

/**
 * 考勤异常率统计VO
 * 用于前端仪表盘展示考勤异常比例
 */
public class AttendanceAnomalyRateVO {

    /** 总记录数 */
    private Long totalCount;
    /** 异常记录数 */
    private Long abnormalCount;
    /** 正常记录数 */
    private Long normalCount;
    /** 异常率（百分比，如 15.5 表示 15.5%） */
    private BigDecimal abnormalRate;

    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
    public Long getAbnormalCount() { return abnormalCount; }
    public void setAbnormalCount(Long abnormalCount) { this.abnormalCount = abnormalCount; }
    public Long getNormalCount() { return normalCount; }
    public void setNormalCount(Long normalCount) { this.normalCount = normalCount; }
    public BigDecimal getAbnormalRate() { return abnormalRate; }
    public void setAbnormalRate(BigDecimal abnormalRate) { this.abnormalRate = abnormalRate; }
}
