package com.buu.oa.enums;

/**
 * 公告状态枚举
 * 对应sys_notice.status字段
 */
public enum NoticeStatus {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    WITHDRAWN(2, "已撤回");

    private final Integer code;
    private final String label;

    NoticeStatus(Integer code, String label) {
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
     * @param code 状态编码
     * @return 对应枚举，未匹配返回null
     */
    public static NoticeStatus fromCode(Integer code) {
        for (NoticeStatus ns : values()) {
            if (ns.code.equals(code)) return ns;
        }
        return null;
    }
}
