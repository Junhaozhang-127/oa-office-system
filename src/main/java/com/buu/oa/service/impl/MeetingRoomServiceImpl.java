package com.buu.oa.service.impl;

import com.buu.oa.entity.MeetingRoom;
import com.buu.oa.mapper.MeetingRoomMapper;
import com.buu.oa.service.MeetingRoomService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议室Service实现
 * 提供会议室基础查询功能
 */
@Service
public class MeetingRoomServiceImpl implements MeetingRoomService {

    private final MeetingRoomMapper meetingRoomMapper;

    public MeetingRoomServiceImpl(MeetingRoomMapper meetingRoomMapper) {
        this.meetingRoomMapper = meetingRoomMapper;
    }

    @Override
    public List<MeetingRoom> getAvailableRooms() {
        return meetingRoomMapper.selectAvailableRooms();
    }

    @Override
    public MeetingRoom getByRoomCode(String roomCode) {
        return meetingRoomMapper.selectByRoomCode(roomCode);
    }
}
