-- 创建数据库
CREATE DATABASE IF NOT EXISTS oa_office DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oa_office;

-- 1. 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(20) NOT NULL COMMENT '部门编码',
    sort INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2. 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(20) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) DEFAULT '' COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password VARCHAR(100) NOT NULL COMMENT '加密密码',
    emp_id BIGINT COMMENT '关联员工ID',
    status TINYINT DEFAULT 1 COMMENT '账号状态：1正常 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 4. 菜单权限表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(100) DEFAULT '' COMMENT '路由路径',
    component VARCHAR(100) DEFAULT '' COMMENT '前端组件路径',
    perms VARCHAR(50) DEFAULT '' COMMENT '权限标识',
    type TINYINT NOT NULL COMMENT '类型：0目录 1菜单 2按钮',
    icon VARCHAR(50) DEFAULT '' COMMENT '菜单图标',
    sort INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 5. 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 6. 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 7. 员工档案表
DROP TABLE IF EXISTS emp_employee;
CREATE TABLE emp_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    emp_no VARCHAR(20) NOT NULL COMMENT '工号',
    name VARCHAR(20) NOT NULL COMMENT '姓名',
    gender TINYINT COMMENT '性别：1男 0女',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    position VARCHAR(50) DEFAULT '' COMMENT '职位',
    entry_date DATE COMMENT '入职日期',
    status TINYINT DEFAULT 1 COMMENT '状态：1在职 2试用期 3离职',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_emp_no (emp_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工档案表';

-- 8. 考勤打卡表
DROP TABLE IF EXISTS attendance_checkin;
CREATE TABLE attendance_checkin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    emp_id BIGINT NOT NULL COMMENT '员工ID',
    check_date DATE NOT NULL COMMENT '打卡日期',
    check_in_time DATETIME COMMENT '上班打卡时间',
    check_out_time DATETIME COMMENT '下班打卡时间',
    status TINYINT NOT NULL COMMENT '状态：1正常 2迟到 3缺卡 4请假',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_emp_date (emp_id, check_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤打卡表';

-- 9. 请假申请表
DROP TABLE IF EXISTS leave_application;
CREATE TABLE leave_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    leave_no VARCHAR(30) NOT NULL COMMENT '请假单号',
    emp_id BIGINT NOT NULL COMMENT '申请人ID',
    leave_type TINYINT NOT NULL COMMENT '类型：1病假 2事假 3年假 4调休',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    days DECIMAL(4,1) NOT NULL COMMENT '请假天数',
    reason VARCHAR(500) DEFAULT '' COMMENT '请假事由',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_leave_no (leave_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 10. 加班申请表
DROP TABLE IF EXISTS overtime_application;
CREATE TABLE overtime_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    overtime_no VARCHAR(30) NOT NULL COMMENT '加班单号',
    emp_id BIGINT NOT NULL COMMENT '申请人ID',
    overtime_type TINYINT NOT NULL COMMENT '类型：1工作日 2周末 3节假日',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    hours DECIMAL(4,1) NOT NULL COMMENT '加班时长(小时)',
    reason VARCHAR(500) DEFAULT '' COMMENT '加班事由',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_overtime_no (overtime_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班申请表';

-- 11. 会议室表
DROP TABLE IF EXISTS meeting_room;
CREATE TABLE meeting_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    room_name VARCHAR(50) NOT NULL COMMENT '会议室名称',
    room_code VARCHAR(20) NOT NULL COMMENT '会议室编号',
    capacity INT DEFAULT 0 COMMENT '容纳人数',
    location VARCHAR(100) DEFAULT '' COMMENT '位置',
    status TINYINT DEFAULT 1 COMMENT '状态：1可用 0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room_code (room_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';

-- 12. 会议预约表
DROP TABLE IF EXISTS meeting_reservation;
CREATE TABLE meeting_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    reservation_no VARCHAR(30) NOT NULL COMMENT '预约单号',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    emp_id BIGINT NOT NULL COMMENT '预约人ID',
    meeting_title VARCHAR(100) NOT NULL COMMENT '会议主题',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    description VARCHAR(500) DEFAULT '' COMMENT '会议说明',
    remind_status TINYINT DEFAULT 0 COMMENT '提醒状态：1已提醒 0未提醒',
    status TINYINT DEFAULT 1 COMMENT '预约状态：1有效 0取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_reservation_no (reservation_no),
    KEY idx_room_time (room_id, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议预约表';

-- 13. 报销单主表
DROP TABLE IF EXISTS expense_report;
CREATE TABLE expense_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_no VARCHAR(30) NOT NULL COMMENT '报销单号',
    emp_id BIGINT NOT NULL COMMENT '报销人ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '报销总金额',
    expense_type VARCHAR(20) NOT NULL COMMENT '报销类型',
    invoice_url VARCHAR(200) DEFAULT '' COMMENT '发票图片地址',
    description VARCHAR(500) DEFAULT '' COMMENT '报销说明',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_report_no (report_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销单主表';

-- 14. 报销明细表
DROP TABLE IF EXISTS expense_report_item;
CREATE TABLE expense_report_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    report_id BIGINT NOT NULL COMMENT '关联报销单ID',
    item_name VARCHAR(100) NOT NULL COMMENT '费用项目',
    amount DECIMAL(10,2) NOT NULL COMMENT '费用金额',
    expense_date DATE COMMENT '发生日期',
    remark VARCHAR(200) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销明细表';

-- 15. 审批记录表
DROP TABLE IF EXISTS approval_record;
CREATE TABLE approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    business_type VARCHAR(20) NOT NULL COMMENT '业务类型：LEAVE/OVERTIME/EXPENSE',
    business_id BIGINT NOT NULL COMMENT '业务单据ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_result TINYINT NOT NULL COMMENT '结果：1通过 2驳回',
    approval_opinion VARCHAR(500) DEFAULT '' COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 16. 公告表
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '富文本内容',
    publisher_id BIGINT COMMENT '发布人ID',
    type TINYINT DEFAULT 1 COMMENT '类型：1通知 2公告',
    status TINYINT DEFAULT 1 COMMENT '状态：1已发布 0草稿',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 17. 公告阅读记录表
DROP TABLE IF EXISTS sys_notice_read;
CREATE TABLE sys_notice_read (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    notice_id BIGINT NOT NULL COMMENT '公告ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_notice_user (notice_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录表';
