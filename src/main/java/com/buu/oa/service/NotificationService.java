package com.buu.oa.service;

import java.util.Map;

/**
 * 消息通知Service
 * 提供通知创建、查询、已读管理
 */
public interface NotificationService {

    /**
     * 创建通知（防重复）
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @param receiverId   接收人ID
     * @param title        标题
     * @param content      内容
     */
    void createNotification(String businessType, Long businessId, Long receiverId,
                            String title, String content);

    /**
     * 批量创建通知给多个接收人
     */
    void createBatchNotification(String businessType, Long businessId,
                                 java.util.List<Long> receiverIds,
                                 String title, String content);

    /**
     * 分页查询我的通知
     */
    Map<String, Object> getMyNotifications(Long receiverId, int page, int size,
                                           String businessType, String status);

    /**
     * 查询未读数量
     */
    int getUnreadCount(Long receiverId);

    /**
     * 标记单条已读
     */
    void markRead(Long id);

    /**
     * 批量标记已读
     */
    void batchMarkRead(java.util.List<Long> ids);

    /**
     * 全部标记已读
     */
    void markAllRead(Long receiverId);
}
