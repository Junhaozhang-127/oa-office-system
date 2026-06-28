package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.entity.ExpenseReport;
import com.buu.oa.entity.ExpenseReportItem;
import com.buu.oa.service.ExpenseReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * 报销单Controller
 * 提供报销单CRUD、发票上传接口
 */
@RestController
@RequestMapping("/api/expense-report")
public class ExpenseReportController {

    private final ExpenseReportService expenseReportService;

    @Value("${server.port:8080}")
    private String serverPort;

    public ExpenseReportController(ExpenseReportService expenseReportService) {
        this.expenseReportService = expenseReportService;
    }

    /**
     * 发票图片上传
     * 接收MultipartFile，保存到本地static/upload/invoice目录，返回访问URL
     * @param file 上传的图片文件
     * @return 上传结果（含图片URL）
     */
    @PostMapping("/upload")
    public R<Map<String, String>> uploadInvoice(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("请选择要上传的文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return R.fail("仅支持图片格式上传");
        }
        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // 保存到项目根目录 uploads/invoice，通过 WebMvcConfig 映射为 /upload/** 访问
            String projectDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(projectDir, "uploads", "invoice");
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(newFileName);
            file.transferTo(targetPath.toFile());

            String url = "/upload/invoice/" + newFileName;
            Map<String, String> result = new LinkedHashMap<>();
            result.put("url", url);
            result.put("fileName", originalName != null ? originalName : newFileName);
            return R.success(result);
        } catch (IOException e) {
            return R.fail("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 创建报销单（含明细行）
     * 请求体：{ empId, expenseType, totalAmount, description, invoiceUrl, items: [{itemName, amount, expenseDate, remark}] }
     */
    @PostMapping("/create")
    public R<ExpenseReport> create(@RequestBody Map<String, Object> body) {
        try {
            Long empId = Long.valueOf(body.get("empId").toString());
            String expenseType = (String) body.get("expenseType");
            BigDecimal totalAmount = new BigDecimal(body.get("totalAmount").toString());
            String description = (String) body.getOrDefault("description", "");
            String invoiceUrl = (String) body.getOrDefault("invoiceUrl", "");

            List<ExpenseReportItem> items = new ArrayList<>();
            Object itemsObj = body.get("items");
            if (itemsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemsObj;
                for (Map<String, Object> itemMap : itemList) {
                    ExpenseReportItem item = new ExpenseReportItem();
                    item.setItemName((String) itemMap.get("itemName"));
                    item.setAmount(new BigDecimal(itemMap.get("amount").toString()));
                    if (itemMap.get("expenseDate") != null) {
                        item.setExpenseDate(LocalDate.parse(itemMap.get("expenseDate").toString()));
                    }
                    item.setRemark((String) itemMap.getOrDefault("remark", ""));
                    items.add(item);
                }
            }

            ExpenseReport report = expenseReportService.createReport(
                    empId, expenseType, totalAmount, description, invoiceUrl, items);
            return R.success(report);
        } catch (Exception e) {
            return R.fail("创建报销单失败：" + e.getMessage());
        }
    }

    /**
     * 查询报销单详情（含明细行）
     */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        ExpenseReport report = expenseReportService.getById(id);
        if (report == null) {
            return R.fail("报销单不存在");
        }
        List<ExpenseReportItem> items = expenseReportService.getItems(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("report", report);
        result.put("items", items);
        return R.success(result);
    }

    /**
     * 查询我的报销单列表
     */
    @GetMapping("/my-list")
    public R<List<ExpenseReport>> myList(@RequestParam Long empId) {
        return R.success(expenseReportService.getByEmpId(empId));
    }

    /**
     * 查询审批列表（按状态筛选，联员工姓名和部门）
     */
    @GetMapping("/approval-list")
    public R<List<Map<String, Object>>> approvalList(@RequestParam(required = false) String status) {
        return R.success(expenseReportService.listWithEmployee(status));
    }

    /**
     * 更新报销单状态
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isEmpty()) {
            return R.fail("状态不能为空");
        }
        expenseReportService.updateStatus(id, status);
        return R.success();
    }

    /**
     * 删除报销单（含明细）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        expenseReportService.deleteReport(id);
        return R.success();
    }
}
