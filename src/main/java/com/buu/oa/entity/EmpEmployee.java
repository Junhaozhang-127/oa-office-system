package com.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工档案实体
 * 映射 emp_employee 表，记录员工基本信息
 */
@Data
@TableName("emp_employee")
public class EmpEmployee {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 工号 */
    private String empNo;
    /** 姓名 */
    private String name;
    /** 性别：1男 0女 */
    private Integer gender;
    /** 所属部门ID */
    private Long deptId;
    /** 职位 */
    private String position;
    /** 入职日期 */
    private LocalDate entryDate;
    /** 状态：1在职 2试用期 3离职 */
    private Integer status;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
