package com.buu.oa.service.impl;

import com.buu.oa.common.RedisKeys;
import com.buu.oa.enums.BusinessType;
import com.buu.oa.service.NotificationService;
import com.buu.oa.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 提醒Service实现
 * 基于Redis ZSet的延迟队列：
 * - ZSet key: oa:reminder:zset
 * - score: 提醒时间戳（毫秒）
 * - value: businessType:businessId:receiverId
 * 定时任务扫描到期提醒，生成Notification后移除
 */
@Service
public class ReminderServiceImpl implements ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderServiceImpl.class);

    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;

    public ReminderServiceImpl(StringRedisTemplate redisTemplate,
                               NotificationService notificationService) {
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }

    @Override
    public void addReminderTask(String businessType, Long businessId, Long receiverId,
                                String title, String content, long remindTimeMs) {
        try {
            String value = buildValue(businessType, businessId, receiverId);
            redisTemplate.opsForZSet().add(RedisKeys.REMINDER_ZSET, value, (double) remindTimeMs);
            log.info("添加提醒任务成功：type={}, businessId={}, receiver={}, remindTime={}",
                    businessType, businessId, receiverId, remindTimeMs);
        } catch (Exception e) {
            log.error("添加提醒任务失败，Redis可能不可用：{}", e.getMessage());
        }
    }

    @Override
    public void cancelReminderTask(String businessType, Long businessId, Long receiverId) {
        try {
            String value = buildValue(businessType, businessId, receiverId);
            redisTemplate.opsForZSet().remove(RedisKeys.REMINDER_ZSET, value);
            log.info("取消提醒任务：type={}, businessId={}, receiver={}", businessType, businessId, receiverId);
        } catch (Exception e) {
            log.error("取消提醒任务失败：{}", e.getMessage());
        }
    }

    @Override
    public void processExpiredReminders() {
        long now = System.currentTimeMillis();
        try {
            // 获取score在0到当前时间的到期任务
            Set<String> expiredTasks = redisTemplate.opsForZSet()
                    .rangeByScore(RedisKeys.REMINDER_ZSET, 0, (double) now);

            if (expiredTasks == null || expiredTasks.isEmpty()) return;

            log.info("扫描到{}个到期提醒任务", expiredTasks.size());

            for (String taskValue : expiredTasks) {
                try {
                    // 解析任务value: businessType:businessId:receiverId
                    String[] parts = taskValue.split(":");
                    if (parts.length < 3) {
                        log.warn("无效的提醒任务格式：{}", taskValue);
                        redisTemplate.opsForZSet().remove(RedisKeys.REMINDER_ZSET, taskValue);
                        continue;
                    }

                    String businessType = parts[0];
                    Long businessId = Long.parseLong(parts[1]);
                    Long receiverId = Long.parseLong(parts[2]);

                    // 生成通知
                    String title = buildReminderTitle(businessType);
                    String content = buildReminderContent(businessType, businessId);
                    notificationService.createNotification(businessType, businessId, receiverId, title, content);

                    // 消费成功后从ZSet移除
                    redisTemplate.opsForZSet().remove(RedisKeys.REMINDER_ZSET, taskValue);
                    log.info("提醒消费成功：{}", taskValue);

                } catch (Exception e) {
                    log.error("处理提醒任务失败：task={}, error={}", taskValue, e.getMessage());
                    // 移除格式错误或处理失败的任务，避免反复消费
                    redisTemplate.opsForZSet().remove(RedisKeys.REMINDER_ZSET, taskValue);
                }
            }
        } catch (Exception e) {
            log.error("扫描到期提醒失败，Redis可能不可用：{}", e.getMessage());
        }
    }

    /**
     * 构建ZSet value: businessType:businessId:receiverId
     */
    private String buildValue(String businessType, Long businessId, Long receiverId) {
        return businessType + ":" + businessId + ":" + receiverId;
    }

    /**
     * 根据业务类型构建提醒标题
     */
    private String buildReminderTitle(String businessType) {
        try {
            BusinessType bt = BusinessType.valueOf(businessType);
            return bt.getLabel() + "提醒";
        } catch (IllegalArgumentException e) {
            return businessType + "提醒";
        }
    }

    /**
     * 根据业务类型构建提醒内容
     */
    private String buildReminderContent(String businessType, Long businessId) {
        switch (businessType) {
            case "MEETING":
                return "您的会议（ID:" + businessId + "）将在15分钟后开始，请准时参加。";
            case "APPROVAL":
                return "您有一条待审批申请（ID:" + businessId + "），请及时处理。";
            case "ANNOUNCEMENT":
                return "有一条新公告发布，请查看详情。";
            default:
                return "您有一条新消息，业务ID:" + businessId;
        }
    }
}
