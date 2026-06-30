# OA协同办公平台

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.15-brightgreen.svg" alt="Spring Boot 3.5.15">
  <img src="https://img.shields.io/badge/MyBatis%20Plus-3.5.7-blue.svg" alt="MyBatis Plus 3.5.7">
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1.svg" alt="MySQL 8.x">
  <img src="https://img.shields.io/badge/Redis-7.x-DC382D.svg" alt="Redis 7.x">
  <img src="https://img.shields.io/badge/license-MIT-green.svg" alt="MIT License">
</p>

企业级OA协同办公平台，基于 Spring Boot 3 + Java 21 + MyBatis-Plus 构建，集成 RBAC 权限管理、多级审批流、JWT 认证等核心办公功能。

## 功能特性

| 模块 | 功能描述 |
|---|---|
| **认证授权** | JWT 无状态认证 + Spring Security + RBAC 五角色权限，29 项按钮级权限控制 |
| **员工管理** | 员工档案 CRUD，分页查询，关联考勤统计 |
| **考勤管理** | 考勤日历视图，月度异常统计，打卡记录 |
| **请假管理** | 请假单提交，支持 7 种请假类型，单级审批流 |
| **加班管理** | 加班单提交，支持 3 种加班类型，单级审批流 |
| **费用报销** | 报销单提交 + 发票图片上传 + 明细行拆分 + EasyExcel 导出 |
| **审批流** | 基于角色的多级审批状态机，报销两级审批（经理→财务），时间线追踪 |
| **会议预约** | 会议室预约 + 时间冲突检测算法 + 预约单号自动生成 |
| **公告通知** | 公告发布/撤回/已读追踪，站内消息推送（Redis ZSet 延迟队列） |
| **仪表盘** | 首页统计卡片：部门人数、待审批数、考勤异常率、报销趋势图 |
| **报表导出** | EasyExcel 多 Sheet 按需导出（员工/考勤/请假/加班/报销/审批） |
| **消息通知** | 站内通知 + 待办提醒 + 会议提醒（定时任务扫描 Redis ZSet） |

## 技术栈

| 层次 | 技术 |
|---|---|
| **后端框架** | Spring Boot 3.5.15, Java 21 |
| **安全认证** | Spring Security, JWT (jjwt 0.12.6, HMAC-SHA 签名) |
| **ORM** | MyBatis-Plus 3.5.7, mybatis-spring 3.0.3 |
| **数据库** | MySQL 8.x |
| **缓存/队列** | Redis 7.x (StringRedisTemplate, ZSet 延迟队列) |
| **文件导出** | Alibaba EasyExcel 3.3.4 |
| **密码加密** | BCrypt (Spring Security BCryptPasswordEncoder) |
| **构建工具** | Maven 3.9+ |
| **前端** | 原生 HTML/CSS/JS (SPA 架构, fetch 拦截器, localStorage JWT 管理) |

## 快速启动

### 环境要求

- JDK 21+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.9+

### 1. 克隆项目

```bash
git clone https://github.com/Junhaozhang-127/oa-office-system.git
cd oa-office-system
```

### 2. 导入数据库

```bash
mysql -u root -p < src/main/resources/init.sql
mysql -u root -p oa_office < src/main/resources/data.sql
```

`init.sql` 创建 `oa_office` 数据库及 21 张业务表，`data.sql` 导入种子数据。

### 3. 修改配置

编辑 `src/main/resources/application.properties`，调整数据库和 Redis 连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/oa_office?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

启动后访问：`http://localhost:8080`

### 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|---|---|---|---|
| `admin` | `123456` | 超级管理员 | 全部权限 |
| `zhangsan` | `123456` | 普通员工 | 基础办公权限 |
| `lisi` | `123456` | 部门经理 | 可审批请假/加班/报销 |
| `sunqi` | `123456` | HR管理员 | 员工档案维护、公告管理 |
| `zhaoliu` | `123456` | 财务管理员 | 报销单终审 |

## 项目结构

```
OA/
├── pom.xml                                    # Maven 构建文件
├── HELP.md                                    # Spring Boot 参考
├── src/main/java/com/buu/oa/
│   ├── OaApplication.java                     # 应用入口 (@EnableScheduling)
│   ├── config/
│   │   ├── SecurityConfig.java                # Spring Security 配置
│   │   ├── RedisConfig.java                   # Redis 配置
│   │   └── WebMvcConfig.java                  # 静态资源映射
│   ├── security/
│   │   ├── JwtUtil.java                       # JWT 生成/解析/校验
│   │   ├── JwtAuthenticationFilter.java       # JWT 认证过滤器
│   │   ├── UserDetailsImpl.java               # 用户详情封装
│   │   ├── UserDetailsServiceImpl.java        # 用户加载服务
│   │   ├── SecurityUtils.java                 # 安全上下文工具类
│   │   └── SecurityAuthHandler.java           # 401/403 异常处理器
│   ├── controller/                            # 14 个 REST Controller
│   │   ├── AuthController.java                # /api/auth
│   │   ├── EmpEmployeeController.java         # /api/employee
│   │   ├── AttendanceController.java          # /api/attendance
│   │   ├── LeaveController.java               # /api/leave-requests
│   │   ├── OvertimeController.java            # /api/overtime-requests
│   │   ├── ApplicationController.java         # /api/applications
│   │   ├── ExpenseReportController.java       # /api/expense-report
│   │   ├── ApprovalController.java            # /api/approval
│   │   ├── MeetingRoomController.java         # /api/meeting-room
│   │   ├── MeetingReservationController.java  # /api/meeting-reservation
│   │   ├── NoticeController.java              # /api/notices
│   │   ├── NotificationController.java        # /api/notifications
│   │   ├── DashboardController.java           # /api/dashboard
│   │   └── ReportController.java              # /api/reports
│   ├── service/                               # 15 个 Service 接口
│   ├── service/impl/                          # 13 个 Service 实现
│   ├── entity/                                # 17 个实体类
│   ├── mapper/                                # 18 个 MyBatis Mapper
│   ├── enums/                                 # 7 个枚举（状态/类型）
│   ├── dto/                                   # 数据传输对象
│   ├── vo/                                    # 视图对象（仪表盘）
│   ├── common/                                # 通用工具（R, RedisKeys）
│   ├── handler/                               # 全局异常处理
│   └── task/                                  # 定时任务（提醒调度）
├── src/main/resources/
│   ├── application.properties                 # 主配置文件
│   ├── init.sql                               # 建库建表脚本（21 张表）
│   ├── data.sql                               # 种子数据（角色/菜单/用户）
│   ├── mapper/                                # 12 个 MyBatis XML 映射文件
│   └── static/                                # 前端静态文件
│       ├── index.html                         # 主页面
│       ├── js/
│       │   ├── auth.js                        # 认证与权限前端模块
│       │   └── app.js                         # 主应用逻辑
│       ├── css/
│       │   └── style.css                      # 样式表
│       └── pages/                             # 子页面（登录/详情）
└── uploads/invoice/                           # 发票上传目录
```

## API 概览

| 模块 | 端点 | 方法 | 说明 |
|---|---|---|---|
| **认证** | `/api/auth/login` | POST | 用户登录 |
| | `/api/auth/me` | GET | 当前用户信息 |
| | `/api/auth/menus` | GET | 当前用户菜单 |
| | `/api/auth/permissions` | GET | 当前用户权限 |
| | `/api/auth/logout` | POST | 退出登录 |
| **员工** | `/api/employee/list` | GET | 员工列表 |
| | `/api/employee/detail` | GET | 员工详情 |
| **考勤** | `/api/attendance/calendar` | GET | 考勤日历 |
| | `/api/attendance/stats` | GET | 月度统计 |
| **请假** | `/api/leave-requests` | POST | 提交请假 |
| **加班** | `/api/overtime-requests` | POST | 提交加班 |
| **申请** | `/api/applications/my` | GET | 我的申请列表 |
| **报销** | `/api/expense-report/upload` | POST | 上传发票 |
| | `/api/expense-report/create` | POST | 创建报销单 |
| | `/api/expense-report/my-list` | GET | 我的报销单 |
| | `/api/expense-report/approval-list` | GET | 待审批报销单 |
| | `/api/expense-report/{id}` | GET | 报销单详情 |
| | `/api/expense-report/{id}/status` | PUT | 更新报销单状态 |
| **审批** | `/api/approval/execute` | POST | 执行审批 |
| | `/api/approval/timeline` | GET | 审批时间线 |
| | `/api/approval/pending-list` | GET | 待审批列表 |
| **会议** | `/api/meeting-room/list` | GET | 会议室列表 |
| | `/api/meeting-reservation/create` | POST | 预约会议 |
| | `/api/meeting-reservation/my-list` | GET | 我的预约 |
| | `/api/meeting-reservation/cancel` | POST | 取消预约 |
| **公告** | `/api/notices` | GET/POST | 公告列表/创建 |
| | `/api/notices/{id}/publish` | POST | 发布公告 |
| | `/api/notices/{id}/read` | POST | 标记已读 |
| | `/api/notices/unread-count` | GET | 未读数 |
| **通知** | `/api/notifications/my` | GET | 我的通知 |
| | `/api/notifications/unread-count` | GET | 未读通知数 |
| | `/api/notifications/read-all` | POST | 全部已读 |
| **仪表盘** | `/api/dashboard/summary` | GET | 首页统计 |
| | `/api/dashboard/leave-type-stats` | GET | 请假类型统计 |
| | `/api/dashboard/reimbursement-trend` | GET | 报销趋势 |
| **报表** | `/api/reports/sheets` | GET | 可用 Sheet 列表 |
| | `/api/reports/export` | POST | 按需导出 |

## 许可证

MIT License

---

**GitHub**: [Junhaozhang-127/oa-office-system](https://github.com/Junhaozhang-127/oa-office-system)
