package com.buu.oa.enums;

/**
 * 加班类型枚举
 * 值与数据库overtime_application.overtime_type字段对应
 */
public enum OvertimeType {

    WORKDAY(1, "工作日加班"),
    WEEKEND(2, "周末加班"),
    HOLIDAY(3, "节假日加班");

    private final Integer code;
    private final String label;

    OvertimeType(Integer code, String label) {
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
    public static OvertimeType fromCode(Integer code) {
        for (OvertimeType ot : values()) {
            if (ot.code.equals(code)) return ot;
        }
        return null;
    }
}
