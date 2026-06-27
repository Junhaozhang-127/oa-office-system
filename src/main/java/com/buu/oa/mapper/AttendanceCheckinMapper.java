package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.AttendanceCheckin;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤打卡Mapper
 * 使用日期范围查询以利用 (emp_id, check_date) 复合索引
 */
public interface AttendanceCheckinMapper extends BaseMapper<AttendanceCheckin> {

    /**
     * 查询员工指定月份的打卡日历数据
     * @param empId     员工ID
     * @param startDate 月份首日（含）
     * @param endDate   次月首日（不含）
     * @return 打卡记录列表
     */
    List<AttendanceCheckin> selectMonthCalendar(@Param("empId") Long empId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 按月统计员工考勤概况
     * @param empId     员工ID
     * @param startDate 月份首日（含）
     * @param endDate   次月首日（不含）
     * @return 考勤统计结果
     */
    java.util.Map<String, Object> selectMonthStats(@Param("empId") Long empId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * 查询员工近期打卡记录
     * @param empId 员工ID
     * @param limit 返回条数
     * @return 近期打卡记录列表，按日期倒序
     */
    List<AttendanceCheckin> selectRecentRecords(@Param("empId") Long empId,
                                                @Param("limit") Integer limit);
}
