package com.buu.oa.service;

import com.buu.oa.entity.ExpenseReport;
import com.buu.oa.entity.ExpenseReportItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报销单Service
 */
public interface ExpenseReportService {

    /**
     * 创建报销单（含明细行）
     * @param empId       报销人ID
     * @param expenseType 报销类型
     * @param totalAmount 报销总金额
     * @param description 报销说明
     * @param invoiceUrl  发票图片地址
     * @param items       明细行列表
     * @return 创建成功的报销单
     */
    ExpenseReport createReport(Long empId, String expenseType, BigDecimal totalAmount,
                               String description, String invoiceUrl, List<ExpenseReportItem> items);

    /**
     * 按ID查询报销单
     * @param id 报销单ID
     * @return 报销单实体
     */
    ExpenseReport getById(Long id);

    /**
     * 查询报销单的明细行
     * @param reportId 报销单ID
     * @return 明细列表
     */
    List<ExpenseReportItem> getItems(Long reportId);

    /**
     * 查询员工的报销单列表
     * @param empId 员工ID
     * @return 报销单列表
     */
    List<ExpenseReport> getByEmpId(Long empId);

    /**
     * 按状态查询报销单（联员工姓名和部门）
     * @param status 审批状态
     * @return 报销单列表
     */
    List<Map<String, Object>> listWithEmployee(String status);

    /**
     * 更新报销单状态
     * @param id     报销单ID
     * @param status 新状态
     */
    void updateStatus(Long id, String status);

    /**
     * 删除报销单（含明细）
     * @param id 报销单ID
     */
    void deleteReport(Long id);
}
