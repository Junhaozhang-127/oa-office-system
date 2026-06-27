package com.buu.oa.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.buu.oa.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表导出Controller
 * 使用EasyExcel实现多Sheet导出，每个Sheet对应一个业务模块
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final DashboardService dashboardService;

    public ReportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 导出多Sheet统计报表
     * 包含：员工信息、考勤记录、请假申请、加班申请、报销记录、审批记录
     * @param startDate 开始日期（可选，默认本月1日）
     * @param endDate   结束日期（可选，默认下月1日）
     */
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       HttpServletResponse response) throws IOException {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        if (start == null) {
            start = LocalDate.now().withDayOfMonth(1);
        }
        if (end == null) {
            end = start.plusMonths(1);
        }

        // 获取导出数据
        Map<String, List<Map<String, Object>>> exportData = dashboardService.getExportData(start, end);

        // 生成文件名：OA统计报表_2026-06-27.xlsx
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String fileName = "OA统计报表_" + today + ".xlsx";

        // 设置响应头，中文文件名URL编码防乱码
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

        // 写入Excel
        int sheetIndex = 0;
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {

            // Sheet1: 员工信息
            writeSheet(excelWriter, sheetIndex++, "员工信息",
                    Arrays.asList("工号", "姓名", "性别", "部门", "职位", "入职日期", "状态", "手机号", "邮箱"),
                    Arrays.asList("empNo", "name", "gender", "deptName", "position", "entryDate", "status", "phone", "email"),
                    exportData.get("employees"));

            // Sheet2: 考勤记录
            writeSheet(excelWriter, sheetIndex++, "考勤记录",
                    Arrays.asList("姓名", "部门", "日期", "上班打卡", "下班打卡", "状态"),
                    Arrays.asList("empName", "deptName", "checkDate", "checkInTime", "checkOutTime", "status"),
                    exportData.get("attendances"));

            // Sheet3: 请假申请
            writeSheet(excelWriter, sheetIndex++, "请假申请",
                    Arrays.asList("单号", "姓名", "部门", "请假类型", "开始日期", "结束日期", "天数", "原因", "状态", "提交时间"),
                    Arrays.asList("leaveNo", "empName", "deptName", "leaveType", "startDate", "endDate", "days", "reason", "status", "createTime"),
                    exportData.get("leaves"));

            // Sheet4: 加班申请
            writeSheet(excelWriter, sheetIndex++, "加班申请",
                    Arrays.asList("单号", "姓名", "部门", "加班类型", "开始时间", "结束时间", "小时数", "原因", "状态", "提交时间"),
                    Arrays.asList("overtimeNo", "empName", "deptName", "overtimeType", "startTime", "endTime", "hours", "reason", "status", "createTime"),
                    exportData.get("overtimes"));

            // Sheet5: 报销记录
            writeSheet(excelWriter, sheetIndex++, "报销记录",
                    Arrays.asList("单号", "姓名", "部门", "报销类型", "金额", "说明", "状态", "提交时间"),
                    Arrays.asList("reportNo", "empName", "deptName", "expenseType", "totalAmount", "description", "status", "createTime"),
                    exportData.get("expenses"));

            // Sheet6: 审批记录
            writeSheet(excelWriter, sheetIndex++, "审批记录",
                    Arrays.asList("业务类型", "业务单ID", "审批人", "审批结果", "审批意见", "审批时间"),
                    Arrays.asList("businessTypeText", "businessId", "approverName", "approvalResult", "approvalOpinion", "approvalTime"),
                    exportData.get("approvals"));
        }

        log.info("报表导出完成：{}，共{}个Sheet", fileName, sheetIndex);
    }

    /**
     * 写入单个Sheet（空数据时仍保留表头）
     * 所有数据值转为字符串写入，避免EasyExcel对java.sql.Date等类型的转换报错
     */
    private void writeSheet(ExcelWriter excelWriter, int sheetNo, String sheetName,
                            List<String> headers, List<String> keys,
                            List<Map<String, Object>> data) {
        List<List<String>> rows = new ArrayList<>();
        // 表头行
        rows.add(new ArrayList<>(headers));
        // 数据行：统一toString处理，避免Date等类型转换异常
        if (data != null) {
            for (Map<String, Object> item : data) {
                List<String> row = new ArrayList<>();
                for (String key : keys) {
                    Object val = item.get(key);
                    row.add(val != null ? val.toString() : "");
                }
                rows.add(row);
            }
        }

        WriteSheet writeSheet = EasyExcel.writerSheet(sheetNo, sheetName).build();
        excelWriter.write(rows, writeSheet);
    }
}
