package com.buu.oa.controller;

import com.buu.oa.common.CurrentUserHelper;
import com.buu.oa.common.R;
import com.buu.oa.entity.Notice;
import com.buu.oa.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 公告Controller
 * 处理公告创建、编辑、发布、撤回、查询、已读标记等前端请求
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 创建公告（草稿状态）
     * @param params {title, content, type, publisherId}
     * @return 创建结果
     */
    @PostMapping
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        try {
            if (params.get("publisherId") == null) {
                params.put("publisherId", CurrentUserHelper.getCurrentUserId());
            }
            Map<String, Object> data = noticeService.createNotice(params);
            return R.success(data);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 编辑公告
     * @param id     公告ID
     * @param params {title, content, type}
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public R<Map<String, Object>> update(@PathVariable Long id,
                                          @RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> data = noticeService.updateNotice(id, params);
            return R.success(data);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 发布公告
     * @param id 公告ID
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    public R<Map<String, Object>> publish(@PathVariable Long id) {
        try {
            Map<String, Object> data = noticeService.publishNotice(id);
            return R.success(data);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 撤回公告
     * @param id 公告ID
     * @return 撤回结果
     */
    @PostMapping("/{id}/withdraw")
    public R<Map<String, Object>> withdraw(@PathVariable Long id) {
        try {
            Map<String, Object> data = noticeService.withdrawNotice(id);
            return R.success(data);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 分页查询公告
     * @param page   页码，默认1
     * @param size   每页条数，默认10
     * @param type   类型，可选(1=通知, 2=公告)
     * @param status 状态，可选(0=草稿, 1=已发布, 2=已撤回)
     * @return 分页结果
     */
    @GetMapping
    public R<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) Integer type,
                                        @RequestParam(required = false) Integer status) {
        Map<String, Object> data = noticeService.getNoticePage(page, size, type, status);
        return R.success(data);
    }

    /**
     * 查询已发布公告（供普通用户浏览）
     */
    @GetMapping("/published")
    public R<Map<String, Object>> published(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) Integer type) {
        Map<String, Object> data = noticeService.getNoticePage(page, size, type, 1);
        return R.success(data);
    }

    /**
     * 查询公告详情
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    public R<Notice> detail(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getNoticeDetail(id);
            return R.success(notice);
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 标记公告已读
     * @param id     公告ID
     * @param params {userId}
     * @return 标记结果
     */
    @PostMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Long userId = getLongParam(params, "userId");
            if (userId == null) userId = CurrentUserHelper.getCurrentUserId();
            noticeService.markRead(id, userId);
            return R.success();
        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 查询用户公告未读数量
     * @param userId 用户ID
     * @return 未读数
     */
    @GetMapping("/unread-count")
    public R<Integer> unreadCount(@RequestParam(required = false) Long userId) {
        if (userId == null) userId = CurrentUserHelper.getCurrentUserId();
        int count = noticeService.getUnreadCount(userId);
        return R.success(count);
    }

    /**
     * 查询用户对指定公告列表的已读状态
     * POST body: {userId, noticeIds: [1,2,3]}
     */
    @PostMapping("/read-status")
    public R<Set<Long>> readStatus(@RequestBody Map<String, Object> params) {
        Long userId = getLongParam(params, "userId");
        if (userId == null) userId = CurrentUserHelper.getCurrentUserId();
        @SuppressWarnings("unchecked")
        List<Long> noticeIds = (List<Long>) params.get("noticeIds");
        if (noticeIds == null) {
            return R.success(java.util.Collections.emptySet());
        }
        Set<Long> readIds = noticeService.getReadNoticeIds(userId, noticeIds);
        return R.success(readIds);
    }

    private Long getLongParam(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
