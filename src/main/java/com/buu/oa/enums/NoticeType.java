package com.buu.oa.enums;

/**
 * 公告类型枚举
 * 对应sys_notice.type字段
 */
public enum NoticeType {

    NOTIFICATION(1, "通知"),
    ANNOUNCEMENT(2, "公告");

    private final Integer code;
    private final String label;

    NoticeType(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public Integer getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 根据code获取枚举
     * @param code 类型编码
     * @return 对应枚举
     */
    public static NoticeType fromCode(Integer code) {
        for (NoticeType nt : values()) {
            if (nt.code.equals(code)) return nt;
        }
        return null;
    }
}
