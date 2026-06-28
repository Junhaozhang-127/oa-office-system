package com.buu.oa.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.buu.oa.common.R;
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
 * 支持多Sheet按需导出，前端通过复选框选择导出模块
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final DashboardService dashboardService;

    /** Sheet定义：key -> {中文名, 表头, 数据key} */
    private static final Map<String, SheetDef> SHEET_DEFS = new LinkedHashMap<>();
    static {
        SHEET_DEFS.put("employees", new SheetDef("员工信息",
                Arrays.asList("工号", "姓名", "性别", "部门", "职位", "入职日期", "状态", "手机号", "邮箱"),
                Arrays.asList("empNo", "name", "gender", "deptName", "position", "entryDate", "status", "phone", "email")));
        SHEET_DEFS.put("attendances", new SheetDef("考勤记录",
                Arrays.asList("姓名", "部门", "日期", "上班打卡", "下班打卡", "状态"),
                Arrays.asList("empName", "deptName", "checkDate", "checkInTime", "checkOutTime", "status")));
        SHEET_DEFS.put("leaves", new SheetDef("请假申请",
                Arrays.asList("单号", "姓名", "部门", "请假类型", "开始日期", "结束日期", "天数", "原因", "状态", "提交时间"),
                Arrays.asList("leaveNo", "empName", "deptName", "leaveType", "startDate", "endDate", "days", "reason", "status", "createTime")));
        SHEET_DEFS.put("overtimes", new SheetDef("加班申请",
                Arrays.asList("单号", "姓名", "部门", "加班类型", "开始时间", "结束时间", "小时数", "原因", "状态", "提交时间"),
                Arrays.asList("overtimeNo", "empName", "deptName", "overtimeType", "startTime", "endTime", "hours", "reason", "status", "createTime")));
        SHEET_DEFS.put("expenses", new SheetDef("报销记录",
                Arrays.asList("单号", "姓名", "部门", "报销类型", "金额", "说明", "状态", "提交时间"),
                Arrays.asList("reportNo", "empName", "deptName", "expenseType", "totalAmount", "description", "status", "createTime")));
        SHEET_DEFS.put("approvals", new SheetDef("审批记录",
                Arrays.asList("业务类型", "业务单ID", "审批人", "审批结果", "审批意见", "审批时间"),
                Arrays.asList("businessTypeText", "businessId", "approverName", "approvalResult", "approvalOpinion", "approvalTime")));
    }

    public ReportController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 获取可导出的Sheet列表（供前端渲染复选框）
     */
    @GetMapping("/sheets")
    public R<List<Map<String, String>>> getAvailableSheets() {
        List<Map<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, SheetDef> entry : SHEET_DEFS.entrySet()) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("key", entry.getKey());
            m.put("name", entry.getValue().name);
            list.add(m);
        }
        return R.success(list);
    }

    /**
     * 按需导出多Sheet报表（POST JSON）
     * 请求体：{ sheets: ["employees","leaves",...], startDate: "2026-06-01", endDate: "2026-06-30" }
     */
    @PostMapping("/export")
    public void exportSelected(@RequestBody Map<String, Object> body,
                                HttpServletResponse response) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> sheets = (List<String>) body.get("sheets");
        if (sheets == null || sheets.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"请至少选择一个导出模块\"}");
            return;
        }

        String startStr = (String) body.get("startDate");
        String endStr = (String) body.get("endDate");
        LocalDate start = startStr != null && !startStr.isEmpty() ? LocalDate.parse(startStr) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endStr != null && !endStr.isEmpty() ? LocalDate.parse(endStr) : start.plusMonths(1);

        // 获取导出数据
        Map<String, List<Map<String, Object>>> exportData = dashboardService.getExportData(start, end);

        // 生成文件名
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String fileName = "OA统计报表_" + today + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

        int sheetIndex = 0;
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            for (String key : sheets) {
                SheetDef def = SHEET_DEFS.get(key);
                if (def == null) continue;
                writeSheet(excelWriter, sheetIndex++, def.name, def.headers, def.keys, exportData.get(key));
            }
        }

        log.info("报表导出完成：{}，共{}个Sheet（选择：{}）", fileName, sheetIndex, sheets);
    }

    /**
     * GET 导出全量报表（保留兼容旧调用）
     */
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       HttpServletResponse response) throws IOException {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : start.plusMonths(1);
        Map<String, List<Map<String, Object>>> exportData = dashboardService.getExportData(start, end);

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String fileName = "OA统计报表_" + today + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

        int sheetIndex = 0;
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            for (Map.Entry<String, SheetDef> entry : SHEET_DEFS.entrySet()) {
                SheetDef def = entry.getValue();
                writeSheet(excelWriter, sheetIndex++, def.name, def.headers, def.keys, exportData.get(entry.getKey()));
            }
        }

        log.info("全量报表导出完成：{}，共{}个Sheet", fileName, sheetIndex);
    }

    private void writeSheet(ExcelWriter excelWriter, int sheetNo, String sheetName,
                            List<String> headers, List<String> keys,
                            List<Map<String, Object>> data) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(new ArrayList<>(headers));
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

    /** Sheet定义内部类 */
    private static class SheetDef {
        final String name;
        final List<String> headers;
        final List<String> keys;

        SheetDef(String name, List<String> headers, List<String> keys) {
            this.name = name;
            this.headers = headers;
            this.keys = keys;
        }
    }
}
