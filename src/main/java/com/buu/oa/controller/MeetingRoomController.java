package com.buu.oa.controller;

import com.buu.oa.common.R;
import com.buu.oa.entity.MeetingRoom;
import com.buu.oa.service.MeetingRoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会议室Controller
 * 提供会议室查询相关接口
 */
@RestController
@RequestMapping("/api/meeting-room")
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    public MeetingRoomController(MeetingRoomService meetingRoomService) {
        this.meetingRoomService = meetingRoomService;
    }

    /**
     * 获取所有可用会议室列表
     * @return 会议室列表
     */
    @GetMapping("/list")
    public R<List<MeetingRoom>> list() {
        List<MeetingRoom> rooms = meetingRoomService.getAvailableRooms();
        return R.success(rooms);
    }

    /**
     * 获取会议室详情
     * @param roomId 会议室ID
     * @return 会议室详情
     */
    @GetMapping("/detail")
    public R<MeetingRoom> detail(@RequestParam Long roomId) {
        return R.success(null);
    }
}
