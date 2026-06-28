package com.buu.oa.service.impl;

import com.buu.oa.entity.Notification;
import com.buu.oa.enums.NotificationStatus;
import com.buu.oa.mapper.NotificationMapper;
import com.buu.oa.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知Service实现
 * 提供通知创建（防重复）、查询、已读管理
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public void createNotification(String businessType, Long businessId, Long receiverId,
                                   String title, String content) {
        // 防重复：同业务同接收人已存在通知则跳过
        int exists = notificationMapper.countByBusiness(businessType, businessId, receiverId);
        if (exists > 0) {
            log.debug("通知已存在，跳过创建：type={}, businessId={}, receiver={}", businessType, businessId, receiverId);
            return;
        }

        Notification notification = new Notification();
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setReceiverId(receiverId);
        notification.setTitle(title);
        notification.setContent(content != null ? content : "");
        notification.setStatus(NotificationStatus.UNREAD.name());
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());

        notificationMapper.insert(notification);
    }

    @Override
    public void createBatchNotification(String businessType, Long businessId,
                                        List<Long> receiverIds, String title, String content) {
        if (receiverIds == null || receiverIds.isEmpty()) return;
        for (Long receiverId : receiverIds) {
            try {
                createNotification(businessType, businessId, receiverId, title, content);
            } catch (Exception e) {
                log.warn("批量创建通知失败：receiver={}, error={}", receiverId, e.getMessage());
            }
        }
    }

    @Override
    public Map<String, Object> getMyNotifications(Long receiverId, int page, int size,
                                                   String businessType, String status) {
        int offset = (page - 1) * size;
        List<Notification> rows = notificationMapper.selectMyNotifications(
                receiverId, businessType, status, offset, size);
        int total = notificationMapper.countMyNotifications(receiverId, businessType, status);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    public int getUnreadCount(Long receiverId) {
        return notificationMapper.countUnread(receiverId);
    }

    @Override
    public void markRead(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) return;
        notification.setStatus(NotificationStatus.READ.name());
        notification.setReadTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.updateById(notification);
    }

    @Override
    public void batchMarkRead(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        notificationMapper.batchMarkRead(ids);
    }

    @Override
    public void markAllRead(Long receiverId) {
        notificationMapper.markAllRead(receiverId);
    }
}
