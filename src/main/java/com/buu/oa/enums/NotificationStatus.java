package com.buu.oa.enums;

/**
 * 消息通知状态枚举
 * 对应oa_notification.status字段
 */
public enum NotificationStatus {

    UNREAD("未读"),
    READ("已读");

    private final String label;

    NotificationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
