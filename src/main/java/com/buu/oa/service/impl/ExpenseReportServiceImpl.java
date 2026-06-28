package com.buu.oa.service.impl;

import com.buu.oa.entity.ExpenseReport;
import com.buu.oa.entity.ExpenseReportItem;
import com.buu.oa.mapper.ExpenseReportItemMapper;
import com.buu.oa.mapper.ExpenseReportMapper;
import com.buu.oa.service.ExpenseReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 报销单Service实现
 */
@Service
public class ExpenseReportServiceImpl implements ExpenseReportService {

    private final ExpenseReportMapper expenseReportMapper;
    private final ExpenseReportItemMapper expenseReportItemMapper;

    public ExpenseReportServiceImpl(ExpenseReportMapper expenseReportMapper,
                                     ExpenseReportItemMapper expenseReportItemMapper) {
        this.expenseReportMapper = expenseReportMapper;
        this.expenseReportItemMapper = expenseReportItemMapper;
    }

    @Override
    @Transactional
    public ExpenseReport createReport(Long empId, String expenseType, BigDecimal totalAmount,
                                      String description, String invoiceUrl,
                                      List<ExpenseReportItem> items) {
        ExpenseReport report = new ExpenseReport();
        report.setReportNo(generateReportNo());
        report.setEmpId(empId);
        report.setExpenseType(expenseType);
        report.setTotalAmount(totalAmount);
        report.setDescription(description != null ? description : "");
        report.setInvoiceUrl(invoiceUrl != null ? invoiceUrl : "");
        report.setStatus("PENDING");
        expenseReportMapper.insert(report);

        if (items != null && !items.isEmpty()) {
            for (ExpenseReportItem item : items) {
                item.setReportId(report.getId());
                expenseReportItemMapper.insert(item);
            }
        }
        return report;
    }

    @Override
    public ExpenseReport getById(Long id) {
        return expenseReportMapper.selectById(id);
    }

    @Override
    public List<ExpenseReportItem> getItems(Long reportId) {
        return expenseReportItemMapper.selectByReportId(reportId);
    }

    @Override
    public List<ExpenseReport> getByEmpId(Long empId) {
        return expenseReportMapper.selectByEmpId(empId);
    }

    @Override
    public List<Map<String, Object>> listWithEmployee(String status) {
        return expenseReportMapper.selectWithEmployee(status);
    }

    @Override
    public void updateStatus(Long id, String status) {
        ExpenseReport report = expenseReportMapper.selectById(id);
        if (report != null) {
            report.setStatus(status);
            expenseReportMapper.updateById(report);
        }
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        expenseReportItemMapper.deleteByReportId(id);
        expenseReportMapper.deleteById(id);
    }

    /**
     * 生成报销单号：BX + yyyyMMdd + 3位序列号
     */
    private String generateReportNo() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int maxSeq = expenseReportMapper.selectMaxSeqByDate(datePrefix);
        return "BX" + datePrefix + String.format("%03d", maxSeq + 1);
    }
}
