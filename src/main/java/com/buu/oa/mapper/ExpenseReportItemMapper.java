package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.ExpenseReportItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报销明细Mapper
 */
public interface ExpenseReportItemMapper extends BaseMapper<ExpenseReportItem> {

    /**
     * 按报销单ID查询明细列表
     * @param reportId 报销单ID
     * @return 明细列表
     */
    List<ExpenseReportItem> selectByReportId(@Param("reportId") Long reportId);

    /**
     * 按报销单ID删除所有明细
     * @param reportId 报销单ID
     * @return 删除条数
     */
    int deleteByReportId(@Param("reportId") Long reportId);
}
