package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.MeetingReservation;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会议预约Mapper
 * 提供会议预约记录的数据操作，含冲突检测查询
 */
public interface MeetingReservationMapper extends BaseMapper<MeetingReservation> {

    /**
     * 检测会议室在指定时间段是否存在冲突预约
     * 重叠判断：已有预约 start_time &lt; 新预约 end_time AND 已有预约 end_time &gt; 新预约 start_time
     * @param roomId    会议室ID
     * @param startTime 新预约开始时间
     * @param endTime   新预约结束时间
     * @return 冲突预约数量，0表示无冲突
     */
    int countConflict(@Param("roomId") Long roomId,
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime);

    /**
     * 按员工ID查询预约记录
     * @param empId 员工ID
     * @return 该员工的全部预约记录列表
     */
    List<MeetingReservation> selectByEmpId(@Param("empId") Long empId);

    /**
     * 查询指定会议室某日的预约列表（联表查会议室名称）
     * @param roomId 会议室ID
     * @param date   日期（yyyy-MM-dd）
     * @return 预约记录列表（含会议室名称）
     */
    List<Map<String, Object>> selectReservationsWithRoom(@Param("roomId") Long roomId,
                                                          @Param("date") String date);

    /**
     * 查询当日最大预约序号，用于生成预约单号
     * @param datePrefix 日期前缀（yyyyMMdd）
     * @return 当日最大序号，无记录返回0
     */
    int selectMaxSeqByDate(@Param("datePrefix") String datePrefix);
}
