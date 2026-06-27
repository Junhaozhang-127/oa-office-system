package com.buu.oa.service;

import com.buu.oa.entity.MeetingRoom;

import java.util.List;

/**
 * 会议室Service
 * 提供会议室基础查询功能
 */
public interface MeetingRoomService {

    /**
     * 获取所有可用会议室
     * @return 状态为可用的会议室列表
     */
    List<MeetingRoom> getAvailableRooms();

    /**
     * 按编号查询会议室
     * @param roomCode 会议室编号
     * @return 会议室实体，未找到返回null
     */
    MeetingRoom getByRoomCode(String roomCode);
}
