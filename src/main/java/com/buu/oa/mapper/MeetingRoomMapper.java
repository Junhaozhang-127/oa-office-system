package com.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buu.oa.entity.MeetingRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会议室Mapper
 * 提供会议室基础数据操作
 */
public interface MeetingRoomMapper extends BaseMapper<MeetingRoom> {

    /**
     * 查询所有可用会议室
     * @return 状态为1（可用）的会议室列表
     */
    List<MeetingRoom> selectAvailableRooms();

    /**
     * 按编号查询会议室
     * @param roomCode 会议室编号
     * @return 会议室实体，未找到返回null
     */
    MeetingRoom selectByRoomCode(@Param("roomCode") String roomCode);
}
