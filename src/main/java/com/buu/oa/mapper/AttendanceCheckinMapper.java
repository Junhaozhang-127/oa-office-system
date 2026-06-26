package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.AttendanceCheckin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考勤打卡Mapper
 * 提供考勤打卡记录的数据库操作
 */
public interface AttendanceCheckinMapper extends BaseMapper<AttendanceCheckin> {

    /**
     * 查询员工指定月份的打卡日历数据
     * 联表查询员工姓名，按日期排序返回当月所有打卡记录
     * @param empId 员工ID
     * @param year  年份
     * @param month 月份
     * @return 打卡记录列表
     */
    List<AttendanceCheckin> selectMonthCalendar(@Param("empId") Long empId,
                                                 @Param("year") Integer year,
                                                 @Param("month") Integer month);

    /**
     * 按月统计员工考勤概况
     * 返回正常、迟到、缺卡、请假各状态计数及出勤天数
     * @param empId 员工ID
     * @param year  年份
     * @param month 月份
     * @return 考勤统计结果
     */
    java.util.Map<String, Object> selectMonthStats(@Param("empId") Long empId,
                                                    @Param("year") Integer year,
                                                    @Param("month") Integer month);
}
