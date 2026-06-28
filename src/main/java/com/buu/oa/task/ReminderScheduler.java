package com.buu.oa.task;

import com.buu.oa.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 提醒定时扫描器
 * 每30秒扫描Redis ZSet中到期的提醒任务，消费后生成Notification
 */
@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 扫描到期提醒任务
     * 固定间隔30秒执行
     */
    @Scheduled(fixedRate = 30000)
    public void scanExpiredReminders() {
        log.debug("开始扫描到期提醒任务...");
        reminderService.processExpiredReminders();
    }
}
