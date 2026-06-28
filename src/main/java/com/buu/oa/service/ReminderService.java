package com.buu.oa.service;

/**
 * 提醒Service
 * 基于Redis ZSet的延迟队列实现
 */
public interface ReminderService {

    /**
     * 添加提醒任务到ZSet延迟队列
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @param receiverId   接收人ID
     * @param title        提醒标题
     * @param content      提醒内容
     * @param remindTime   提醒时间（毫秒时间戳）
     */
    void addReminderTask(String businessType, Long businessId, Long receiverId,
                         String title, String content, long remindTime);

    /**
     * 取消提醒任务
     * @param businessType 业务类型
     * @param businessId   业务ID
     * @param receiverId   接收人ID
     */
    void cancelReminderTask(String businessType, Long businessId, Long receiverId);

    /**
     * 扫描并消费到期提醒（由定时任务调用）
     * 获取score <= 当前时间戳的任务，生成通知后从ZSet移除
     */
    void processExpiredReminders();
}
