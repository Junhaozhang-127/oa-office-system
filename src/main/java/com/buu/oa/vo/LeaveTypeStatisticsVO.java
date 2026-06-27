package com.buu.oa.vo;

/**
 * 请假类型统计VO
 * 用于前端饼图展示各请假类型占比
 */
public class LeaveTypeStatisticsVO {

    /** 请假类型编码 */
    private Integer leaveType;
    /** 请假类型名称 */
    private String leaveTypeName;
    /** 申请数量 */
    private Long count;

    public Integer getLeaveType() { return leaveType; }
    public void setLeaveType(Integer leaveType) { this.leaveType = leaveType; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
