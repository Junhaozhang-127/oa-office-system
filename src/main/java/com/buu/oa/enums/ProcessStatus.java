package com.buu.oa.enums;

/**
 * 流程审批状态枚举
 * 统一管理请假、加班、报销等模块的审批状态
 */
public enum ProcessStatus {

    PENDING("待审批"),
    MANAGER_APPROVED("经理已审批"),
    FINANCE_APPROVED("财务已审批"),
    REJECTED("已驳回"),
    COMPLETED("已完成");

    private final String label;

    ProcessStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 根据状态字符串获取枚举
     * @param name 状态名称
     * @return 对应枚举，未匹配时返回null
     */
    public static ProcessStatus fromName(String name) {
        for (ProcessStatus ps : values()) {
            if (ps.name().equalsIgnoreCase(name)) return ps;
        }
        return null;
    }
}
