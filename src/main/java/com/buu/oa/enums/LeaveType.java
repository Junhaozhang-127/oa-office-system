package com.buu.oa.enums;

/**
 * 请假类型枚举
 * 值与数据库leave_application.leave_type字段对应
 */
public enum LeaveType {

    SICK_LEAVE(1, "病假"),
    PERSONAL_LEAVE(2, "事假"),
    ANNUAL_LEAVE(3, "年假"),
    COMPENSATORY_LEAVE(4, "调休"),
    MARRIAGE_LEAVE(5, "婚假"),
    MATERNITY_LEAVE(6, "产假"),
    OTHER(7, "其他");

    private final Integer code;
    private final String label;

    LeaveType(Integer code, String label) {
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
     * 根据数据库code获取枚举
     * @param code 类型编码
     * @return 对应枚举
     */
    public static LeaveType fromCode(Integer code) {
        for (LeaveType lt : values()) {
            if (lt.code.equals(code)) return lt;
        }
        return null;
    }
}
