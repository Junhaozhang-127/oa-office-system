package com.buu.oa.enums;

/**
 * 消息业务类型枚举
 * 对应oa_notification.business_type字段
 */
public enum BusinessType {

    ANNOUNCEMENT("公告"),
    MEETING("会议提醒"),
    APPROVAL("审批待办"),
    LEAVE("请假"),
    OVERTIME("加班"),
    REIMBURSEMENT("报销");

    private final String label;

    BusinessType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
