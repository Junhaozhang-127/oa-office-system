package com.buu.oa.service.impl;

import com.buu.oa.entity.Notice;
import com.buu.oa.entity.NoticeRead;
import com.buu.oa.enums.NoticeStatus;
import com.buu.oa.mapper.EmpEmployeeMapper;
import com.buu.oa.mapper.NoticeMapper;
import com.buu.oa.mapper.NoticeReadMapper;
import com.buu.oa.service.NoticeService;
import com.buu.oa.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 公告Service实现
 * 核心功能：公告CRUD、发布/撤回、已读状态管理、公告发布后通知全站用户
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    private static final Logger log = LoggerFactory.getLogger(NoticeServiceImpl.class);

    private final NoticeMapper noticeMapper;
    private final NoticeReadMapper noticeReadMapper;
    private final NotificationService notificationService;
    private final EmpEmployeeMapper empEmployeeMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper,
                             NoticeReadMapper noticeReadMapper,
                             NotificationService notificationService,
                             EmpEmployeeMapper empEmployeeMapper) {
        this.noticeMapper = noticeMapper;
        this.noticeReadMapper = noticeReadMapper;
        this.notificationService = notificationService;
        this.empEmployeeMapper = empEmployeeMapper;
    }

    @Override
    public Map<String, Object> createNotice(Map<String, Object> params) {
        String title = getString(params, "title");
        String content = getString(params, "content");
        Integer type = getInt(params, "type");
        Long publisherId = getLong(params, "publisherId");

        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("公告标题不能为空");
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("公告内容不能为空");
        if (type == null) type = 2;
        if (publisherId == null) throw new IllegalArgumentException("发布人ID不能为空");

        Notice notice = new Notice();
        notice.setTitle(title.trim());
        notice.setContent(content);
        notice.setType(type);
        notice.setPublisherId(publisherId);
        notice.setStatus(NoticeStatus.DRAFT.getCode());
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());

        noticeMapper.insert(notice);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", notice.getId());
        result.put("title", notice.getTitle());
        result.put("status", NoticeStatus.DRAFT.getCode());
        result.put("statusText", NoticeStatus.DRAFT.getLabel());
        return result;
    }

    @Override
    public Map<String, Object> updateNotice(Long id, Map<String, Object> params) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) throw new IllegalArgumentException("公告不存在");
        if (!NoticeStatus.DRAFT.getCode().equals(notice.getStatus()))
            throw new IllegalArgumentException("仅草稿状态的公告可编辑");

        String title = getString(params, "title");
        String content = getString(params, "content");
        Integer type = getInt(params, "type");

        if (title != null && !title.trim().isEmpty()) notice.setTitle(title.trim());
        if (content != null && !content.trim().isEmpty()) notice.setContent(content);
        if (type != null) notice.setType(type);
        notice.setUpdateTime(LocalDateTime.now());

        noticeMapper.updateById(notice);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", notice.getId());
        result.put("updated", true);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> publishNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) throw new IllegalArgumentException("公告不存在");
        if (NoticeStatus.WITHDRAWN.getCode().equals(notice.getStatus()))
            throw new IllegalArgumentException("已撤回的公告不可发布");

        notice.setStatus(NoticeStatus.PUBLISHED.getCode());
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);

        // 公告发布后，为所有用户生成通知
        try {
            List<Long> allEmpIds = empEmployeeMapper.selectAllEmpIds();
            if (allEmpIds != null && !allEmpIds.isEmpty()) {
                notificationService.createBatchNotification(
                        "ANNOUNCEMENT", id, allEmpIds,
                        "公告：" + notice.getTitle(),
                        truncateContent(notice.getContent(), 100));
                log.info("公告发布成功，已通知{}位用户，公告ID={}", allEmpIds.size(), id);
            }
        } catch (Exception e) {
            log.warn("公告发布成功，但生成通知失败，公告ID={}，错误={}", id, e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("status", NoticeStatus.PUBLISHED.getCode());
        result.put("statusText", NoticeStatus.PUBLISHED.getLabel());
        return result;
    }

    @Override
    public Map<String, Object> withdrawNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) throw new IllegalArgumentException("公告不存在");
        if (!NoticeStatus.PUBLISHED.getCode().equals(notice.getStatus()))
            throw new IllegalArgumentException("仅已发布状态的公告可撤回");

        notice.setStatus(NoticeStatus.WITHDRAWN.getCode());
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("status", NoticeStatus.WITHDRAWN.getCode());
        result.put("statusText", NoticeStatus.WITHDRAWN.getLabel());
        return result;
    }

    @Override
    public Map<String, Object> getNoticePage(int page, int size, Integer type, Integer status) {
        int offset = (page - 1) * size;
        List<Notice> rows = noticeMapper.selectNoticePage(offset, size, type, status);
        int total = noticeMapper.countNotice(type, status);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public Notice getNoticeDetail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) throw new IllegalArgumentException("公告不存在");
        return notice;
    }

    @Override
    public void markRead(Long noticeId, Long userId) {
        NoticeRead existing = noticeReadMapper.selectByNoticeAndUser(noticeId, userId);
        if (existing != null) return; // 已读不重复记录

        NoticeRead record = new NoticeRead();
        record.setNoticeId(noticeId);
        record.setUserId(userId);
        record.setReadTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        noticeReadMapper.insert(record);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return noticeReadMapper.countUnreadByUser(userId);
    }

    @Override
    public Set<Long> getReadNoticeIds(Long userId, List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) return Collections.emptySet();
        List<Long> readIds = noticeReadMapper.selectReadNoticeIds(userId, noticeIds);
        return new HashSet<>(readIds != null ? readIds : Collections.emptyList());
    }

    /**
     * 截断富文本内容用于通知摘要
     */
    private String truncateContent(String content, int maxLen) {
        if (content == null) return "";
        String plainText = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        if (plainText.length() <= maxLen) return plainText;
        return plainText.substring(0, maxLen) + "...";
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
