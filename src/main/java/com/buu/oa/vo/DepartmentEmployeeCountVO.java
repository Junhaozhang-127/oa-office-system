package com.buu.oa.vo;

/**
 * 部门人数统计VO
 * 用于前端柱状图展示各部门员工数量
 */
public class DepartmentEmployeeCountVO {

    /** 部门ID */
    private Long departmentId;
    /** 部门名称 */
    private String departmentName;
    /** 员工数量 */
    private Long employeeCount;

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Long employeeCount) { this.employeeCount = employeeCount; }
}
