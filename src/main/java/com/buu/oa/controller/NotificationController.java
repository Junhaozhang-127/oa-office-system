package com.buu.oa.controller;

import com.buu.oa.common.CurrentUserHelper;
import com.buu.oa.common.R;
import com.buu.oa.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息通知Controller
 * 处理消息通知查询、已读标记等前端请求
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 查询我的通知列表
     * @param receiverId   接收人ID
     * @param page         页码
     * @param size         每页条数
     * @param businessType 业务类型，可选
     * @param status       消息状态，可选
     * @return 分页结果
     */
    @GetMapping("/my")
    public R<Map<String, Object>> myNotifications(
            @RequestParam(required = false) Long receiverId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String status) {
        if (receiverId == null) receiverId = CurrentUserHelper.getCurrentUserId();
        Map<String, Object> data = notificationService.getMyNotifications(
                receiverId, page, size, businessType, status);
        return R.success(data);
    }

    /**
     * 查询未读消息数量
     * @param receiverId 接收人ID
     * @return 未读数
     */
    @GetMapping("/unread-count")
    public R<Integer> unreadCount(@RequestParam(required = false) Long receiverId) {
        if (receiverId == null) receiverId = CurrentUserHelper.getCurrentUserId();
        int count = notificationService.getUnreadCount(receiverId);
        return R.success(count);
    }

    /**
     * 标记单条通知已读
     * @param id 通知ID
     * @return 标记结果
     */
    @PostMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return R.success();
    }

    /**
     * 批量标记已读
     * @param params {ids: [1,2,3]}
     * @return 标记结果
     */
    @PostMapping("/batch-read")
    public R<Void> batchMarkRead(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) params.get("ids");
        notificationService.batchMarkRead(ids);
        return R.success();
    }

    /**
     * 全部标记已读
     * @param receiverId 接收人ID
     * @return 标记结果
     */
    @PostMapping("/read-all")
    public R<Void> markAllRead(@RequestParam(required = false) Long receiverId) {
        if (receiverId == null) receiverId = CurrentUserHelper.getCurrentUserId();
        notificationService.markAllRead(receiverId);
        return R.success();
    }
}
